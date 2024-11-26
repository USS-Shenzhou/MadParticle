package cn.ussshenzhou.madparticle.particle.bloom;

import cn.ussshenzhou.madparticle.particle.ModParticleShaders;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.fml.loading.FMLLoader;
import org.lwjgl.opengl.GL46;

import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * do multi down-sampler, each level will have half width and height than the level before.
 * do blur at one axis and then another to improve performance.
 * different levels is similar to the mipmap, but by this way we can use different convolution kernel
 */
public class BloomManager implements AutoCloseable {

    public static final boolean DEBUG = !FMLLoader.isProduction();
    public static final BloomManager INSTANCE = new BloomManager(4);

    private final int downSamplerCount;
    /**
     * store data that should go through the bloom pipeline
     */
    private final RenderTarget inputTarget;
    /**
     * store data that is intended to check a pixel should be bloom or not
     */
    private final RenderTarget bloomMaskTarget;
    /**
     * we will attach {@link MainTarget#getColorTextureId()} to {@link GL46#GL_COLOR_ATTACHMENT1}
     * avoid clearing it and remember to re-attach when resized
     */
    private final RenderTarget oitOutputTarget;
    /**
     * due to openGL don't allow a texture read and write at a single draw call
     * we draw data to this and then blit it
     */
    private final RenderTarget compositeTarget;
    private final RenderTarget[] downSamplerTexturesHorizon;
    private final RenderTarget[] downSamplerTexturesVertical;

    /**
     * MeshData will be closed after upload, so re-create it everytime
     */
    private static final Supplier<MeshData> MESH_DATA = () -> {
        var builder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(-1, 1, 0);
        builder.addVertex(-1, -1, 0);
        builder.addVertex(1, -1, 0);
        builder.addVertex(1, 1, 0);
        return builder.buildOrThrow();
    };

    /**
     * Maybe there is a better way to support any down-sampler in composite shader at one time.
     * currently only support exactly 4
     */
    private BloomManager(int downSamplerCount) {
        this(downSamplerCount, Minecraft.getInstance().getMainRenderTarget());
    }

    private BloomManager(int downSamplerCount, RenderTarget bloomSizeTarget) {
        this(downSamplerCount, bloomSizeTarget.width, bloomSizeTarget.height);
    }

    private BloomManager(int downSamplerCount, int width, int height) {
        var currentFrameBuffer = GL46.glGetInteger(GL46.GL_FRAMEBUFFER_BINDING);
        this.downSamplerCount = downSamplerCount <= 0 ? 4 : downSamplerCount;
        this.inputTarget = createRenderTarget(width, height);
        this.compositeTarget = createRenderTarget(width, height);
        this.bloomMaskTarget = createRenderTarget(width, height);
        this.oitOutputTarget = createRenderTarget(width, height);
        this.downSamplerTexturesHorizon = IntStream.iterate(2, i -> i * 2)
                .limit(downSamplerCount)
                .mapToObj(factor -> createRenderTarget(width / factor, height / factor))
                .toArray(RenderTarget[]::new);
        this.downSamplerTexturesVertical = IntStream.iterate(2, i -> i * 2)
                .limit(downSamplerCount)
                .mapToObj(factor -> createRenderTarget(width / factor, height / factor))
                .toArray(RenderTarget[]::new);
        this.attachMainColorAttachment();

        //these will help you in debug tools
        if (DEBUG) {
            GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, inputTarget.frameBufferId, "bloomInput");
            GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, compositeTarget.frameBufferId, "bloomComposite");
            GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, bloomMaskTarget.frameBufferId, "bloomMask");
            GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, oitOutputTarget.frameBufferId, "bloomOitOutput");
            for (int i = 0; i < downSamplerCount; i++) {
                RenderTarget samplerTexture = downSamplerTexturesHorizon[i];
                GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, samplerTexture.frameBufferId,
                        "bloomProcessHorizon" + (i + 1));
            }
            for (int i = 0; i < downSamplerCount; i++) {
                RenderTarget samplerTexture = downSamplerTexturesVertical[i];
                GL46.glObjectLabel(GL46.GL_FRAMEBUFFER, samplerTexture.frameBufferId,
                        "bloomProcessVertical" + (i + 1));
            }
        }
        //state should be recovered
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, currentFrameBuffer);
    }

    private static RenderTarget createRenderTarget(int width, int height) {
        var renderTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        renderTarget.setFilterMode(GL46.GL_LINEAR);
        return renderTarget;
    }

    /**
     * oit output should also output none-bloom original image,
     * as we only bloom image data that requires bloom
     */
    private void attachMainColorAttachment() {
        this.oitOutputTarget.bindWrite(false);
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT1, GL46.GL_TEXTURE_2D,
                Minecraft.getInstance().getMainRenderTarget().getColorTextureId(), 0);
        GL46.glDrawBuffers(new int[]{GL46.GL_COLOR_ATTACHMENT0, GL46.GL_COLOR_ATTACHMENT1});
    }

    public void resize(int newWidth, int newHeight) {
        this.inputTarget.resize(newWidth, newHeight, Minecraft.ON_OSX);
        this.compositeTarget.resize(newWidth, newHeight, Minecraft.ON_OSX);
        this.bloomMaskTarget.resize(newWidth, newHeight, Minecraft.ON_OSX);
        this.oitOutputTarget.resize(newWidth, newHeight, Minecraft.ON_OSX);
        IntStream.range(0, downSamplerCount).forEach(i -> {
            var factor = (int) Math.pow(2, i);
            this.downSamplerTexturesHorizon[i].resize(newWidth / factor, newHeight / factor, Minecraft.ON_OSX);
            this.downSamplerTexturesVertical[i].resize(newWidth / factor, newHeight / factor, Minecraft.ON_OSX);
        });
        //need re-attach, after resize colorAttachment(texture)'is will change
        this.attachMainColorAttachment();
    }

    public RenderTarget getInputTarget() {
        return this.inputTarget;
    }

    public RenderTarget getBloomMaskTarget() {
        return this.bloomMaskTarget;
    }

    public RenderTarget getOitOutputTarget() {
        return this.oitOutputTarget;
    }

    public void doBloom() {
        //assume the needed bloom information is all in inputTarget
        var downSamplerShader = ModParticleShaders.getDownSamplerShader();
        //process bloom
        for (int i = 0; i < this.downSamplerCount; i++) {
            var sourceRenderTarget = i == 0 ? this.inputTarget : this.downSamplerTexturesHorizon[i - 1];
            var targetRenderTarget = this.downSamplerTexturesHorizon[i];

            float outWidth = (float) targetRenderTarget.width;
            float outHeight = (float) targetRenderTarget.height;

            targetRenderTarget.bindWrite(true);
            GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);

            downSamplerShader.setSampler("DiffuseSampler", sourceRenderTarget.getColorTextureId());
            downSamplerShader.safeGetUniform("OutSize")
                    .set(outWidth, outHeight);
            downSamplerShader.safeGetUniform("BlurDir")
                    .set(1.0f, 0.0f);
            downSamplerShader.safeGetUniform("Radius").
                    set((i + 1) * 7 + 1);

            RenderSystem.setShader(ModParticleShaders::getDownSamplerShader);
            BufferUploader.drawWithShader(MESH_DATA.get());

            sourceRenderTarget = targetRenderTarget;
            targetRenderTarget = this.downSamplerTexturesVertical[i];

            targetRenderTarget.bindWrite(true);
            GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);
            downSamplerShader.setSampler("DiffuseSampler", sourceRenderTarget.getColorTextureId());
            downSamplerShader.safeGetUniform("BlurDir")
                    .set(0.0f, 1.0f);
            BufferUploader.drawWithShader(MESH_DATA.get());
        }

        //do composite
        var compositeShader = ModParticleShaders.getBloomCompositeShader();
        this.setupBloomTexturesForComposite(compositeShader);
        compositeShader.setSampler("DiffuseSampler", Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
        compositeShader.setSampler("HighLight", inputTarget.getColorTextureId());

        compositeShader.safeGetUniform("BloomRadius").set(1.0f);
        compositeShader.safeGetUniform("BloomIntensive").set(1.7f);

        compositeTarget.bindWrite(true);
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);

        RenderSystem.setShader(ModParticleShaders::getBloomCompositeShader);
        BufferUploader.drawWithShader(MESH_DATA.get());
        this.shutdownBloomTexturesForComposite(compositeShader);

        //do blit
        GL46.glBindFramebuffer(GL46.GL_READ_FRAMEBUFFER, compositeTarget.frameBufferId);
        GL46.glBindFramebuffer(GL46.GL_DRAW_FRAMEBUFFER, Minecraft.getInstance().getMainRenderTarget().frameBufferId);
        var width = compositeTarget.width;
        var height = compositeTarget.height;
        GL46.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                GL46.GL_COLOR_BUFFER_BIT, GL46.GL_LINEAR);
    }

    /**
     * {@link GL46#GL_TEXTURE_2D_ARRAY} requires all textures at same size, can't use it here
     **/
    private void setupBloomTexturesForComposite(ShaderInstance compositeShader) {
        GL46.glUseProgram(compositeShader.getId());
        var index = compositeShader.samplerLocations.size() + 1;
        var activeTexture = GL46.glGetInteger(GL46.GL_ACTIVE_TEXTURE);
        for (int i = 0; i < this.downSamplerTexturesHorizon.length; i++) {
            var location = GL46.glGetUniformLocation(compositeShader.getId(), "BlurTexture" + (i + 1));
            GL46.glUniform1i(location, index + i);
            GL46.glActiveTexture(GL46.GL_TEXTURE0 + index + i);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, this.downSamplerTexturesVertical[i].getColorTextureId());
        }
        GL46.glActiveTexture(activeTexture);
    }

    private void shutdownBloomTexturesForComposite(ShaderInstance compositeShader) {
        var index = compositeShader.samplerLocations.size() + 1;
        var activeTexture = GL46.glGetInteger(GL46.GL_ACTIVE_TEXTURE);
        for (int i = 0; i < this.downSamplerTexturesHorizon.length; i++) {
            GL46.glActiveTexture(GL46.GL_TEXTURE0 + index + i);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);
        }
        GL46.glActiveTexture(activeTexture);
    }

    @Override
    public void close() {
        this.inputTarget.destroyBuffers();
        this.compositeTarget.destroyBuffers();
        this.bloomMaskTarget.destroyBuffers();
        for (var downSamplerTexture : this.downSamplerTexturesHorizon) {
            downSamplerTexture.destroyBuffers();
        }
        for (var downSamplerTexture : this.downSamplerTexturesVertical) {
            downSamplerTexture.destroyBuffers();
        }
    }

    /**
     * assume candidate Bloom information in {@link  BloomManager#oitOutputTarget}
     * assume bloom stencil data stored in {@link  BloomManager#bloomMaskTarget}
     */
    public void extractOIT() {
        var oitExtractShader = ModParticleShaders.getOitExtractShader();
        oitExtractShader.setSampler("BloomMask", this.bloomMaskTarget.getColorTextureId());
        oitExtractShader.setSampler("OitOutput", this.oitOutputTarget.getColorTextureId());

        this.inputTarget.bindWrite(true);
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);

        RenderSystem.setShader(ModParticleShaders::getOitExtractShader);
        BufferUploader.drawWithShader(MESH_DATA.get());

        this.bloomMaskTarget.bindWrite(true);
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);
    }

    /**
     * when fill vertex data, we should let extraLight/bloomFactor greater than 1,
     * in shader now, greater than 1.001f
     */
    public boolean isBloomOn() {
        return true;
    }

    /**
     * @param colorAttachment like {@link GL46#GL_COLOR_ATTACHMENT2}
     */
    public void attachBloomMaskTargetToOit(int colorAttachment) {
        int textureId = this.getBloomMaskTarget().getColorTextureId();
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, colorAttachment,
                GL46.GL_TEXTURE_2D, textureId, 0);
    }
}
