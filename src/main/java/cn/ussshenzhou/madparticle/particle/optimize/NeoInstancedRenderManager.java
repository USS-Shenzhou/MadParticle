package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.madparticle.util.LightCache;
import cn.ussshenzhou.madparticle.util.SimpleBlockPos;
import cn.ussshenzhou.t88.T88;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.ussshenzhou.madparticle.particle.optimize.MultiThreadHelper.*;
import static org.lwjgl.opengl.ARBInstancedArrays.*;
import static org.lwjgl.opengl.GL42.*;

/**
 * @author USS_Shenzhou
 */
public class NeoInstancedRenderManager {
    private final ResourceLocation usingAtlas;

    public NeoInstancedRenderManager(ResourceLocation usingAtlas) {
        this.usingAtlas = usingAtlas;
    }
    //----------meta manager----------

    /**
     * 0 is for {@link net.minecraft.client.renderer.texture.TextureAtlas#LOCATION_PARTICLES},
     * <br>
     * 1 is for {@link net.minecraft.client.renderer.texture.TextureAtlas#LOCATION_BLOCKS}.
     */
    @SuppressWarnings("deprecation")
    private static final NeoInstancedRenderManager[] MANAGER_BY_RENDER_TYPE = new NeoInstancedRenderManager[]{
            new NeoInstancedRenderManager(TextureAtlas.LOCATION_PARTICLES),
            new NeoInstancedRenderManager(TextureAtlas.LOCATION_BLOCKS),
    };

    public static NeoInstancedRenderManager getInstance(ParticleRenderType renderType) {
        if (renderType == ParticleRenderType.TERRAIN_SHEET || renderType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
            return MANAGER_BY_RENDER_TYPE[1];
        }
        return MANAGER_BY_RENDER_TYPE[0];
    }

    public static Stream<NeoInstancedRenderManager> getAllInstances() {
        return Stream.of(MANAGER_BY_RENDER_TYPE);
    }

    public static void forEach(Consumer<? super NeoInstancedRenderManager> consumer) {
        consumer.accept(MANAGER_BY_RENDER_TYPE[0]);
        consumer.accept(MANAGER_BY_RENDER_TYPE[1]);
    }

    public static void init() {
        LogUtils.getLogger().info("NeoInstancedRenderManager inited.");
    }

    //----------render----------
    /**
     * <pre>{@code
     * //-----per tick update-----
     * //single float
     * layout (location=0) in vec4 instanceXYZRoll;
     * //single float
     * layout (location=1) in vec4 prevInstanceXYZRoll;
     * //half float
     * layout (location=2) in vec4 instanceUV;
     * //half float
     * layout (location=3) in vec4 instanceColor;
     * //half float
     * layout (location=4) in vec2 sizeExtraLight;
     * //(4+4 bits) 1 byte + 3 byte padding
     * layout (location=5) in uint instanceUV2;
     * }</pre>
     */
    private static final int TICK_VBO_SIZE = 8 * 4 + 4 * 2 + 4 * 2 + 2 * 2 + 4;
    private static final LightCache LIGHT_CACHE = new LightCache();
    private static boolean forceMaxLight = false;
    private static final GpuBuffer PROXY_VAO = ModRenderPipelines.INSTANCED_COMMON_DEPTH.getVertexFormat().uploadImmediateVertexBuffer(ByteBuffer.allocateDirect(128));
    private static final GpuBuffer EBO;
    private static final short DEFAULT_EXTRA_LIGHT = Float.floatToFloat16(1f);

    private final PersistentMappedArrayBuffer tickVBO = new PersistentMappedArrayBuffer();
    private int amount, nextAmount;
    private volatile CompletableFuture<Void> updateTickVBOTask = null;
    private final MappableRingBuffer cameraCorrectionUbo = new MappableRingBuffer(() -> "MadParticle CameraCorrection Uniform",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            (4 + 4) * 4);

    static {
        NeoForge.EVENT_BUS.addListener(NeoInstancedRenderManager::checkForceMaxLight);
        var eboBuffer = BufferUtils.createByteBuffer(6 * 4);
        eboBuffer.putInt(0);
        eboBuffer.putInt(1);
        eboBuffer.putInt(2);
        eboBuffer.putInt(2);
        eboBuffer.putInt(1);
        eboBuffer.putInt(3);
        eboBuffer.flip();
        EBO = ModRenderPipelines.INSTANCED_COMMON_DEPTH.getVertexFormat().uploadImmediateIndexBuffer(eboBuffer);
    }

    @SubscribeEvent
    public static void checkForceMaxLight(ClientTickEvent.Pre event) {
        forceMaxLight = ConfigHelper.getConfigRead(MadParticleConfig.class).forceMaxLight;
    }

    public void render() {
        if (amount == 0) {
            return;
        }
        var mc = Minecraft.getInstance();
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var cameraUbo = encoder.mapBuffer(cameraCorrectionUbo.currentBuffer(), false, true);
        var dynamicUbo = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 0.0F);
        //noinspection DataFlowIssue
        var pass = encoder.createRenderPass(
                () -> "MadParticle render",
                mc.getMainRenderTarget().getColorTextureView(),
                OptionalInt.empty(),
                mc.getMainRenderTarget().getDepthTextureView(),
                OptionalDouble.empty());
        try (pass; cameraUbo) {
            pass.setPipeline(getRenderPipeline());
            setUniform(pass, mc, cameraUbo, dynamicUbo);
            setVAO(pass);
            tickVBO.getCurrent().bind();
            pass.setIndexBuffer(EBO, VertexFormat.IndexType.INT);
            bindIrisFBO();
            pass.drawIndexed(0, 0, 6, amount);
        }
        cleanUp();
    }

    public void preUpdate() {
        if (updateTickVBOTask != null) {
            updateTickVBOTask.join();
            tickVBO.next();
        }
    }

    public void update(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles) {
        amount = nextAmount;
        nextAmount = particles.size();
        tickVBO.ensureCapacity(TICK_VBO_SIZE * particles.size());
    }

    public void postUpdate(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles) {
        updateTickVBOTask = executeUpdate(particles, this::updateTickVBOInternal, tickVBO);
    }

    private RenderPipeline getRenderPipeline() {
        return switch (ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod) {
            case DEPTH_TRUE -> ModRenderPipelines.INSTANCED_COMMON_DEPTH;
            case DEPTH_FALSE -> ModRenderPipelines.INSTANCED_COMMON_BLEND;
            default -> ModRenderPipelines.INSTANCED_COMMON_DEPTH;
        };
    }

    private void setVAO(RenderPass pass) {
        pass.setVertexBuffer(0, PROXY_VAO);
        ((GlDevice) RenderSystem.getDevice()).vertexArrayCache().bindVertexArray(getRenderPipeline().getVertexFormat(), (GlBuffer) PROXY_VAO);
        setVertexAttributeArray();
    }

    private void setUniform(RenderPass pass, Minecraft mc, GpuBuffer.MappedView ubo, GpuBufferSlice dynamicUbo) {
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", dynamicUbo);
        var camera = mc.gameRenderer.getMainCamera();
        var cameraPos = camera.getPosition();
        var cameraRot = camera.rotation();
        Std140Builder.intoBuffer(ubo.data())
                .putVec4(cameraRot.x, cameraRot.y, cameraRot.z, cameraRot.w)
                .putVec4((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        pass.setUniform("CameraCorrection", cameraCorrectionUbo.currentBuffer());
        pass.bindSampler("Sampler0", mc.getTextureManager().getTexture(usingAtlas).getTextureView());
        pass.bindSampler("Sampler2", mc.gameRenderer.lightTexture().getTextureView());
    }

    private void updateTickVBOInternal(ObjectLinkedOpenHashSet<TextureSheetParticle> particles, int index, long tickVBOAddress, @Deprecated float partialTicks) {
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        for (TextureSheetParticle particle : particles) {
            long start = tickVBOAddress + (long) index * TICK_VBO_SIZE;
            //xyz roll
            MemoryUtil.memPutFloat(start, (float) particle.x);
            MemoryUtil.memPutFloat(start + 4, (float) particle.y);
            MemoryUtil.memPutFloat(start + 8, (float) particle.z);
            MemoryUtil.memPutFloat(start + 12, particle.roll);
            //prev xyz roll
            MemoryUtil.memPutFloat(start + 16, (float) particle.xo);
            MemoryUtil.memPutFloat(start + 20, (float) particle.yo);
            MemoryUtil.memPutFloat(start + 24, (float) particle.zo);
            MemoryUtil.memPutFloat(start + 28, particle.oRoll);
            //uv
            MemoryUtil.memPutShort(start + 32, Float.floatToFloat16(particle.getU0()));
            MemoryUtil.memPutShort(start + 34, Float.floatToFloat16(particle.getU1()));
            MemoryUtil.memPutShort(start + 36, Float.floatToFloat16(particle.getV0()));
            MemoryUtil.memPutShort(start + 38, Float.floatToFloat16(particle.getV1()));
            //color
            MemoryUtil.memPutShort(start + 40, Float.floatToFloat16(particle.rCol));
            MemoryUtil.memPutShort(start + 42, Float.floatToFloat16(particle.gCol));
            MemoryUtil.memPutShort(start + 44, Float.floatToFloat16(particle.bCol));
            MemoryUtil.memPutShort(start + 46, Float.floatToFloat16(particle.alpha));
            //size extraLight
            MemoryUtil.memPutShort(start + 48, Float.floatToFloat16(particle.getQuadSize(0.5f)));
            if (particle instanceof MadParticle madParticle) {
                MemoryUtil.memPutShort(start + 50, Float.floatToFloat16(madParticle.getBloomFactor()));
            } else {
                MemoryUtil.memPutShort(start + 50, DEFAULT_EXTRA_LIGHT);
            }
            //uv2
            if (forceMaxLight) {
                MemoryUtil.memPutByte(start + 52, (byte) 0xff);
            } else {
                float x = (float) (particle.xo + 0.5f * (particle.x - particle.xo));
                float y = (float) (particle.yo + 0.5f * (particle.y - particle.yo));
                float z = (float) (particle.zo + 0.5f * (particle.z - particle.zo));
                simpleBlockPosSingle.set(Mth.floor(x), Mth.floor(y), Mth.floor(z));
                byte l;
                if (particle instanceof MadParticle madParticle) {
                    l = LIGHT_CACHE.getOrCompute(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z, particle, simpleBlockPosSingle);
                    l = madParticle.checkEmit(l);
                } else if (TakeOver.RENDER_CUSTOM_LIGHT.contains(particle.getClass())) {
                    l = LightCache.compressPackedLight(particle.getLightColor(partialTicks));
                } else {
                    l = LIGHT_CACHE.getOrCompute(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z, particle, simpleBlockPosSingle);
                }
                MemoryUtil.memPutByte(start + 52, l);
            }
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> executeUpdate(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles, VboUpdater updater, PersistentMappedArrayBuffer vbo) {
        var partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        CompletableFuture<Void>[] futures = new CompletableFuture[getThreads()];
        int index = 0;
        for (int group = 0; group < futures.length; group++) {
            @SuppressWarnings("rawtypes")
            ObjectLinkedOpenHashSet set = particles.get(group);
            int i = index;
            futures[group] = CompletableFuture.runAsync(
                    () -> updater.update((ObjectLinkedOpenHashSet<TextureSheetParticle>) set, i, vbo.getNext().getAddress(), partialTicks),
                    getFixedThreadPool()
            );
            index += set.size();
        }
        return CompletableFuture.allOf(futures);
    }

    private void setVertexAttributeArray() {
        tickVBO.getCurrent().bind();
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, TICK_VBO_SIZE, 0);
        glVertexAttribDivisorARB(0, 1);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, TICK_VBO_SIZE, 16);
        glVertexAttribDivisorARB(1, 1);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 32);
        glVertexAttribDivisorARB(2, 1);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 40);
        glVertexAttribDivisorARB(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 48);
        glVertexAttribDivisorARB(4, 1);

        glEnableVertexAttribArray(5);
        glVertexAttribIPointer(5, 1, GL_UNSIGNED_BYTE, TICK_VBO_SIZE, 52);
        glVertexAttribDivisorARB(5, 1);
    }

    private void cleanUp() {
        cameraCorrectionUbo.rotate();
        GlStateManager._glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDepthMask(true);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getAmount() {
        return amount;
    }

    @FunctionalInterface
    public interface VboUpdater {
        void update(ObjectLinkedOpenHashSet<TextureSheetParticle> particles, int startIndex, long frameVBOAddress, float partialTicks);
    }

    //----------iris----------
    private void bindIrisFBO() {
        if (!cn.ussshenzhou.madparticle.MadParticle.irisOn) {
            return;
        }
        var program = ((GlDevice) RenderSystem.getDevice()).getOrCompilePipeline(getRenderType().renderPipeline).program();
        try {
            var writingToBeforeTranslucentField = program.getClass().getDeclaredField("writingToAfterTranslucent");
            writingToBeforeTranslucentField.setAccessible(true);
            var writingToBeforeTranslucent = writingToBeforeTranslucentField.get(program);
            var bindMethod = writingToBeforeTranslucent.getClass().getDeclaredMethod("bind");
            bindMethod.setAccessible(true);
            bindMethod.invoke(writingToBeforeTranslucent);
            glDrawBuffer(GL_COLOR_ATTACHMENT0);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LogUtils.getLogger().error(e.toString());
        }
    }

    private RenderType.CompositeRenderType getRenderType() {
        return (RenderType.CompositeRenderType) (usingAtlas == TextureAtlas.LOCATION_BLOCKS ? ParticleRenderType.TERRAIN_SHEET.renderType() : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.renderType());
    }
}
