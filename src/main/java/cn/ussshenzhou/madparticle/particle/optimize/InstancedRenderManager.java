package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.ModParticleShaders;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.madparticle.util.LightCache;
import cn.ussshenzhou.t88.T88;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.event.ResizeHudEvent;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL40C.*;

/**
 * @author USS_Shenzhou
 */
public class InstancedRenderManager {
    public static final int INSTANCE_UV_INDEX = 0;
    public static final int INSTANCE_XYZ_INDEX = INSTANCE_UV_INDEX + 3;

    public static final int SIZE_FLOAT_OR_INT_BYTES = 4;
    public static final int AMOUNT_INSTANCE_FLOATS = 4 + 4 + 4 + 3;
    public static final int SIZE_INSTANCE_BYTES = AMOUNT_INSTANCE_FLOATS * SIZE_FLOAT_OR_INT_BYTES;
    public static final float[] ACCUM_INIT = {0, 0, 0, 0};
    public static final float[] REVEAL_INIT = {1};

    private static int threads = Mth.clamp(ConfigHelper.getConfigRead(MadParticleConfig.class).bufferFillerThreads, 1, Integer.MAX_VALUE);
    @SuppressWarnings("unchecked")
    private static LinkedHashSet<TextureSheetParticle>[] PARTICLES = Stream.generate(() -> Sets.newLinkedHashSetWithExpectedSize(32768)).limit(threads).toArray(LinkedHashSet[]::new);
    private static Executor fixedThreadPool = Executors.newFixedThreadPool(threads);
    private static final LightCache LIGHT_CACHE = new LightCache();
    private static boolean forceMaxLight = false;
    private static final int VAO, EBO, OIT_FBO, ACCUM_TEXTURE, REVEAL_TEXTURE, POST_VAO, POST_VBO;

    static {
        NeoForge.EVENT_BUS.addListener(InstancedRenderManager::checkForceMaxLight);
        NeoForge.EVENT_BUS.addListener(InstancedRenderManager::onWindowResize);
        VAO = glGenVertexArrays();
        EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        var ebo = new int[]{
                0, 1, 2,
                2, 3, 0
        };
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ebo, GL_STATIC_DRAW);
        OIT_FBO = glGenFramebuffers();
        ACCUM_TEXTURE = glGenTextures();
        REVEAL_TEXTURE = glGenTextures();
        resetOitTexture();
        var window = Minecraft.getInstance().getWindow();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, window.getWidth(), window.getHeight(), 0, GL_RGBA, GL_HALF_FLOAT, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, OIT_FBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, ACCUM_TEXTURE, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, REVEAL_TEXTURE, 0);
        var translucentDrawBuffers = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
        glDrawBuffers(translucentDrawBuffers);
        POST_VAO = glGenVertexArrays();
        POST_VBO = glGenBuffers();
        glBindVertexArray(POST_VAO);
        glBindBuffer(GL_ARRAY_BUFFER, POST_VBO);
        var quadVertices = new float[]{
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f
        };
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glBindVertexArray(0);
    }

    private static void resetOitTexture() {
        var window = Minecraft.getInstance().getWindow();
        glBindTexture(GL_TEXTURE_2D, ACCUM_TEXTURE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, window.getWidth(), window.getHeight(), 0, GL_RGBA, GL_HALF_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, REVEAL_TEXTURE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R16, window.getWidth(), window.getHeight(), 0, GL_RGBA, GL_HALF_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, OIT_FBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, Minecraft.getInstance().getMainRenderTarget().getDepthTextureId(), 0);
    }

    /**
     * Manual reg.
     */
    @SubscribeEvent
    public static void onWindowResize(ResizeHudEvent event) {
        resetOitTexture();
    }

    /**
     * Manual reg.
     */
    @SubscribeEvent
    public static void checkForceMaxLight(ClientTickEvent.Pre event) {
        forceMaxLight = ConfigHelper.getConfigRead(MadParticleConfig.class).forceMaxLight;
    }

    @SuppressWarnings("unchecked")
    public static void setThreads(int amount) {
        if (amount <= 0 || amount > 128) {
            throw new IllegalArgumentException("The amount of auxiliary threads should between 1 and 128. Correct the config file manually.");
        }
        threads = amount;
        var p = PARTICLES;
        PARTICLES = Stream.generate(() -> Sets.newLinkedHashSetWithExpectedSize(32768)).limit(threads).toArray(LinkedHashSet[]::new);
        Arrays.stream(p).forEach(set -> set.forEach(InstancedRenderManager::add));
        fixedThreadPool = Executors.newFixedThreadPool(threads);
    }

    public static Executor getFixedThreadPool() {
        return fixedThreadPool;
    }

    public static int getThreads() {
        return threads;
    }

    private static HashSet<TextureSheetParticle> findSmallestSet() {
        HashSet<TextureSheetParticle> r = PARTICLES[0];
        int minSize = r.size();
        for (int i = 1; i < threads; i++) {
            if (PARTICLES[i].size() < minSize) {
                r = PARTICLES[i];
                minSize = r.size();
            }
        }
        return r;
    }

    public static void add(TextureSheetParticle particle) {
        findSmallestSet().add(particle);
    }

    public static void reload(Collection<Particle> particles) {
        clear();
        particles.forEach(p -> add((TextureSheetParticle) p));
    }

    public static void remove(TextureSheetParticle particle) {
        for (int i = 0; i < threads; i++) {
            if (PARTICLES[i].remove(particle)) {
                break;
            }
        }
    }

    public static void removeAll(Collection<Particle> particle) {
        //Arrays.stream(PARTICLES).forEach(set -> set.removeAll(particle));
        particle.stream().filter(p -> p instanceof TextureSheetParticle).forEach(p -> remove((TextureSheetParticle) p));
    }

    public static void clear() {
        Arrays.stream(PARTICLES).forEach(HashSet::clear);
    }

    public static int amount() {
        int size = 0;
        for (int i = 0; i < threads; i++) {
            size += PARTICLES[i].size();
        }
        return size;
    }

    public static void render(Camera camera, float partialTicks, Frustum clippingHelper, TextureManager textureManager) {
        if (amount() == 0) {
            return;
        }
        //-----prepare var
        long instanceMatrixBufferBaseAddress = MemoryUtil.getAllocator(false).calloc(1, (long) amount() * SIZE_INSTANCE_BYTES);
        int amount;
        prepareShader(textureManager);
        //-----fill vbo
        if (threads <= 1) {
            amount = renderSync(instanceMatrixBufferBaseAddress, camera, partialTicks, clippingHelper);
        } else {
            amount = renderAsync(instanceMatrixBufferBaseAddress, camera, partialTicks, clippingHelper);
        }
        //-----set opengl state
        glBindVertexArray(VAO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        int instanceMatrixBufferId = bindBuffer(instanceMatrixBufferBaseAddress);
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;
        prepareFinal(camera, shader);
        //-----draw
        GL31C.glDrawElementsInstanced(4, 6, GL11C.GL_UNSIGNED_INT, 0, amount);
        if (ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod == TranslucentMethod.OIT) {
            oitPost();
        } else {
            RenderSystem.depthMask(true);
        }
        //-----done and clean up
        shader.clear();
        cleanUp(instanceMatrixBufferBaseAddress, instanceMatrixBufferId);
    }

    private static void prepareShader(TextureManager textureManager) {
        if (ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod == TranslucentMethod.OIT) {
            ModParticleRenderTypes.INSTANCED_OIT.begin(Tesselator.getInstance(), textureManager);
            glBindFramebuffer(GL_FRAMEBUFFER, OIT_FBO);
            glClearBufferfv(GL_COLOR, 0, ACCUM_INIT);
            glClearBufferfv(GL_COLOR, 1, REVEAL_INIT);
        } else {
            ModParticleRenderTypes.INSTANCED.begin(Tesselator.getInstance(), textureManager);
        }
    }

    @SuppressWarnings("unchecked")
    public static int renderAsync(long instanceMatrixBufferBaseAddress, Camera camera, float partialTicks, Frustum clippingHelper) {
        CompletableFuture<Void>[] futures = new CompletableFuture[threads];
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> partial(finalI, instanceMatrixBufferBaseAddress, partialTicks, clippingHelper),
                    fixedThreadPool
            );
        }
        CompletableFuture.allOf(futures).join();
        return amount();
    }

    private static void partial(int group, long buffer, float partialTicks, Frustum clippingHelper) {
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        var set = PARTICLES[group];
        int index = 0;
        for (int i = 0; i < group; i++) {
            index += PARTICLES[i].size();
        }
        for (TextureSheetParticle particle : set) {
            fillBuffer(buffer, particle, index, partialTicks, simpleBlockPosSingle);
            index++;
        }
    }

    public static int renderSync(long instanceMatrixBuffer, Camera camera, float partialTicks, Frustum clippingHelper) {
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        int amount = 0;
        for (TextureSheetParticle particle : PARTICLES[0]) {
            if (clippingHelper != null && !clippingHelper.isVisible(particle.getRenderBoundingBox(partialTicks)) && !clippingHelper.isVisible(particle.getBoundingBox())) {
                continue;
            }
            fillBuffer(instanceMatrixBuffer, particle, amount, partialTicks, simpleBlockPosSingle);
            amount++;
        }
        return amount;
    }

    /**
     * HOTSPOT
     * Can you find a way to make it faster?
     */
    public static void fillBuffer(long buffer, TextureSheetParticle particle, int index, float partialTicks, SimpleBlockPos simpleBlockPosSingle) {
        long start = buffer + (long) index * SIZE_INSTANCE_BYTES;
        //uv
        var sprite = particle.sprite;
        MemoryUtil.memPutFloat(start, sprite.getU0());
        MemoryUtil.memPutFloat(start + 4, sprite.getU1());
        MemoryUtil.memPutFloat(start + 4 * 2, sprite.getV0());
        MemoryUtil.memPutFloat(start + 4 * 3, sprite.getV1());
        //color
        MemoryUtil.memPutFloat(start + 4 * 4, particle.rCol);
        MemoryUtil.memPutFloat(start + 4 * 5, particle.gCol);
        MemoryUtil.memPutFloat(start + 4 * 6, particle.bCol);
        MemoryUtil.memPutFloat(start + 4 * 7, particle.alpha);
        //uv2
        float x = Mth.lerp(partialTicks, (float) particle.xo, (float) particle.x);
        float y = Mth.lerp(partialTicks, (float) particle.yo, (float) particle.y);
        float z = Mth.lerp(partialTicks, (float) particle.zo, (float) particle.z);
        if (forceMaxLight) {
            MemoryUtil.memPutFloat(start + 4 * 8, 240f);
        } else {
            simpleBlockPosSingle.set(Mth.floor(x), Mth.floor(y), Mth.floor(z));
            int l;
            if (particle instanceof MadParticle madParticle) {
                l = LIGHT_CACHE.getOrCompute(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z, particle, simpleBlockPosSingle);
                l = madParticle.checkEmit(l);
            } else if (TakeOver.RENDER_CUSTOM_LIGHT.contains(particle.getClass())) {
                l = particle.getLightColor(partialTicks);
            } else {
                l = LIGHT_CACHE.getOrCompute(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z, particle, simpleBlockPosSingle);
            }
            MemoryUtil.memPutFloat(start + 4 * 8, (float) (l & 0x0000_ffff));
            MemoryUtil.memPutFloat(start + 4 * 9, (float) (l >> 16 & 0x0000_ffff));
        }
        //size/roll
        MemoryUtil.memPutFloat(start + 4 * 10, particle.getQuadSize(partialTicks));
        MemoryUtil.memPutFloat(start + 4 * 11, Mth.lerp(partialTicks, particle.oRoll, particle.roll));
        //xyz
        MemoryUtil.memPutFloat(start + 4 * 12, x);
        MemoryUtil.memPutFloat(start + 4 * 13, y);
        MemoryUtil.memPutFloat(start + 4 * 14, z);
    }

    public static void prepareFinal(Camera camera, ShaderInstance shader) {
        prepareSamplerAndUniform(camera, shader);
        RenderSystem.setupShaderLights(shader);
        shader.apply();
        if (cn.ussshenzhou.madparticle.MadParticle.IS_OPTIFINE_INSTALLED) {
            //TODO if optifine shader using
            glUseProgram(shader.getId());
        }
        //TODO need update
        if (cn.ussshenzhou.madparticle.MadParticle.irisOn) {
            //Borrow iris particle shader's frame buffer.
            //Profiler tells me this is ok. We should trust JVM.
            try {
                ShaderInstance translucent = GameRenderer.getParticleShader();
                Class<? extends ShaderInstance> translucentClass = translucent.getClass();
                Field writingToAfterTranslucent = translucentClass.getDeclaredField("writingToAfterTranslucent");
                writingToAfterTranslucent.setAccessible(true);
                Object irisGlFramebuffer = writingToAfterTranslucent.get(translucent);
                Field id = irisGlFramebuffer.getClass().getSuperclass().getDeclaredField("id");
                id.setAccessible(true);
                int frameBuffer = (int) id.get(irisGlFramebuffer);
                glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
            } catch (Exception e) {
                if (T88.TEST) {
                    LogUtils.getLogger().error("{}", e.getMessage());
                }
            }
            GL11C.glDepthMask(true);
            GL11C.glColorMask(true, true, true, true);
        }
    }

    private static void prepareSamplerAndUniform(Camera camera, ShaderInstance shader) {
        for (int i1 = 0; i1 < 12; ++i1) {
            int textureId = RenderSystem.getShaderTexture(i1);
            shader.setSampler("Sampler" + i1, textureId);
        }

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        }

        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }

        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        var pos = camera.getPosition().toVector3f();
        //noinspection DataFlowIssue
        shader.getUniform("CamXYZ").set(pos);
        var quat = camera.rotation();
        //noinspection DataFlowIssue
        shader.getUniform("CamQuat").set(quat.x, quat.y, quat.z, quat.w);
    }

    private static void oitPost() {
        glBindFramebuffer(GL_FRAMEBUFFER, Minecraft.getInstance().getMainRenderTarget().frameBufferId);
        RenderSystem.setShader(ModParticleShaders::getInstancedParticleShaderOitPost);
        glEnable(GL_BLEND);
        glDepthMask(true);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        assert RenderSystem.getShader() != null;
        RenderSystem.getShader().setSampler("accum", ACCUM_TEXTURE);
        RenderSystem.getShader().setSampler("reveal", REVEAL_TEXTURE);
        RenderSystem.getShader().apply();
        glBindVertexArray(POST_VAO);
        glBindBuffer(GL_ARRAY_BUFFER, POST_VBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public static int bindBuffer(long buffer) {
        int bufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, MemoryUtil.memByteBuffer(buffer, amount() * SIZE_INSTANCE_BYTES), GL_STREAM_DRAW);
        int formerSize = 0;
        for (int i = 0; i < 3; i++) {
            glEnableVertexAttribArray(INSTANCE_UV_INDEX + i);
            glVertexAttribPointer(INSTANCE_UV_INDEX + i, 4, GL11C.GL_FLOAT, false, SIZE_INSTANCE_BYTES, formerSize);
            formerSize += 4 * SIZE_FLOAT_OR_INT_BYTES;
            glVertexAttribDivisor(INSTANCE_UV_INDEX + i, 1);
        }
        glEnableVertexAttribArray(INSTANCE_XYZ_INDEX);
        glVertexAttribPointer(INSTANCE_XYZ_INDEX, 3, GL11C.GL_FLOAT, false, SIZE_INSTANCE_BYTES, formerSize);
        glVertexAttribDivisor(INSTANCE_XYZ_INDEX, 1);
        return bufferId;
    }

    public static void cleanUp(long instanceMatrixBuffer, int instanceMatrixBufferId) {
        for (int i = 0; i < 3; i++) {
            glDisableVertexAttribArray(INSTANCE_UV_INDEX + i);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDeleteBuffers(instanceMatrixBufferId);
        glBindVertexArray(0);
        MemoryUtil.getAllocator(false).free(instanceMatrixBuffer);
    }

    public static class SimpleBlockPos {
        public int x, y, z;

        public SimpleBlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public SimpleBlockPos copy() {
            return new SimpleBlockPos(x, y, z);
        }

        @Override
        public int hashCode() {
            return (y + z * 31) * 37 + x;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof SimpleBlockPos pos)) {
                return false;
            } else {
                return this.x == pos.x && this.y == pos.y && this.z == pos.z;
            }
        }
    }
}
