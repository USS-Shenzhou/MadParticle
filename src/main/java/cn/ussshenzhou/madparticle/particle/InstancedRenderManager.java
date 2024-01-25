package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class InstancedRenderManager {
    public static final int INSTANCE_UV_INDEX = 2;
    public static final int INSTANCE_COLOR_INDEX = 3;
    public static final int INSTANCE_UV2_INDEX = 4;
    public static final int INSTANCE_MATRIX_INDEX = 5;

    public static final int SIZE_FLOAT_OR_INT_BYTES = 4;
    public static final int AMOUNT_MATRIX_FLOATS = 4 * 4;
    public static final int AMOUNT_INSTANCE_FLOATS = 4 + 4 + (2 + 2) + AMOUNT_MATRIX_FLOATS;
    public static final int SIZE_INSTANCE_BYTES = AMOUNT_INSTANCE_FLOATS * SIZE_FLOAT_OR_INT_BYTES;

    private static final LinkedHashSet<TextureSheetParticle> PARTICLES = Sets.newLinkedHashSetWithExpectedSize(32768);
    private static int threads = ConfigHelper.getConfigRead(MadParticleConfig.class).bufferFillerThreads;
    private static Executor fixedThreadPool = Executors.newFixedThreadPool(threads);
    @SuppressWarnings("unchecked")
    private static HashMap<SimpleBlockPos, Integer>[] lightCaches = Stream.generate(() -> new HashMap<String, Integer>(16384)).limit(threads).toArray(HashMap[]::new);

    @SuppressWarnings("unchecked")
    public static void setThreads(int amount) {
        if (amount < 0 || amount > 128) {
            throw new IllegalArgumentException("The amount of auxiliary threads should between 1 and 128. Correct the config file manually.");
        }
        threads = amount;
        lightCaches = Stream.generate(() -> new HashMap<String, Integer>(16384)).limit(threads).toArray(HashMap[]::new);
        fixedThreadPool = Executors.newFixedThreadPool(threads);
    }

    public static Executor getFixedThreadPool() {
        return fixedThreadPool;
    }

    public static int getThreads() {
        return threads;
    }

    public static void add(TextureSheetParticle particle) {
        PARTICLES.add(particle);
    }

    public static void reload(Collection<Particle> particles) {
        PARTICLES.clear();
        particles.forEach(p -> add((TextureSheetParticle) p));
    }

    public static void remove(TextureSheetParticle particle) {
        PARTICLES.remove(particle);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static void removeAll(Collection<Particle> particle) {
        PARTICLES.removeAll(particle);
    }

    public static void clear() {
        PARTICLES.clear();
    }

    public static int amount() {
        return PARTICLES.size();
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float partialTicks, Frustum clippingHelper, TextureManager textureManager) {
        if (PARTICLES.isEmpty()) {
            return;
        }
        InstancedRenderBufferBuilder bufferBuilder = ModParticleRenderTypes.instancedRenderBufferBuilder;
        ModParticleRenderTypes.INSTANCED.begin(bufferBuilder, textureManager);
        ByteBuffer instanceMatrixBuffer = MemoryUtil.memAlloc(PARTICLES.size() * SIZE_INSTANCE_BYTES);
        MemoryUtil.memSet(instanceMatrixBuffer, 0);
        int amount;
        //TODO add an option of checking visibility
        if (threads <= 1) {
            amount = renderSync(instanceMatrixBuffer, camera, partialTicks, clippingHelper);
        } else {
            amount = renderAsync(instanceMatrixBuffer, camera, partialTicks, clippingHelper);
        }
        fillVertices(bufferBuilder);
        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        var vertexBuffer = BufferUploader.upload(renderedBuffer);
        if (cn.ussshenzhou.madparticle.MadParticle.IS_OPTIFINE_INSTALLED) {
            //noinspection DataFlowIssue
            GL30C.glBindVertexArray(vertexBuffer.arrayObjectId);
            GL20C.glEnableVertexAttribArray(1);
            GL30C.glVertexAttribIPointer(1, 4, GL11C.GL_INT, 28, 3 * 4);
        }
        //noinspection DataFlowIssue
        int instanceMatrixBufferId = bindBuffer(instanceMatrixBuffer, vertexBuffer.arrayObjectId);
        ShaderInstance shader = RenderSystem.getShader();
        prepare(shader);
        GL31C.glDrawElementsInstanced(4, 6,
                RenderSystem.sharedSequentialQuad.hasStorage(65536) ? GL11C.GL_UNSIGNED_INT : GL11C.GL_UNSIGNED_SHORT,
                0, amount);
        //noinspection DataFlowIssue
        shader.clear();
        end(instanceMatrixBuffer, instanceMatrixBufferId);
        Arrays.stream(lightCaches).forEach(HashMap::clear);
    }

    @SuppressWarnings("unchecked")
    public static int renderAsync(ByteBuffer instanceMatrixBuffer, Camera camera, float partialTicks, Frustum clippingHelper) {
        var camPosCompensate = camera.getPosition().toVector3f().mul(-1);
        CompletableFuture<Void>[] futures = new CompletableFuture[threads];
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> partial(lightCaches[finalI], finalI * (PARTICLES.size() / threads),
                            PARTICLES.size() / threads + (finalI == threads - 1 ? PARTICLES.size() % threads : 0),
                            instanceMatrixBuffer, partialTicks, camera, camPosCompensate, clippingHelper),
                    fixedThreadPool
            );
        }
        CompletableFuture.allOf(futures).join();
        return PARTICLES.size();
    }

    private static void partial(HashMap<SimpleBlockPos, Integer> lightCache, int start, int length, ByteBuffer buffer, float partialTicks, Camera camera, Vector3f camPosCompensate, Frustum clippingHelper) {
        Matrix4f matrix4f = new Matrix4f();
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        var iterator = PARTICLES.iterator();
        for (int i = 0; i < start + length && iterator.hasNext(); i++) {
            if (i < start) {
                iterator.next();
                continue;
            }
            var particle = iterator.next();
            fillBuffer(lightCache, buffer, particle, i, partialTicks, matrix4f, camera, camPosCompensate, simpleBlockPosSingle);
        }
    }

    public static int renderSync(ByteBuffer instanceMatrixBuffer, Camera camera, float partialTicks, Frustum clippingHelper) {
        Matrix4f matrix4f = new Matrix4f();
        var camPosCompensate = camera.getPosition().toVector3f().mul(-1);
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        int amount = 0;
        for (TextureSheetParticle particle : PARTICLES) {
            if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) {
                continue;
            }
            fillBuffer(lightCaches[threads - 1], instanceMatrixBuffer, particle, amount, partialTicks, matrix4f, camera, camPosCompensate, simpleBlockPosSingle);
            amount++;
        }
        return amount;
    }

    private static int getLight(TextureSheetParticle particle, BlockPos pos) {
        return particle.level.hasChunkAt(pos) ? LevelRenderer.getLightColor(particle.level, pos) : 0;
    }

    /**
     * HOTSPOT
     * Can you find a way to make it faster?
     */
    public static void fillBuffer(HashMap<SimpleBlockPos, Integer> lightCache, ByteBuffer buffer, TextureSheetParticle particle, int i, float partialTicks, Matrix4f matrix4fSingle, Camera camera, Vector3f camPosCompensate, SimpleBlockPos simpleBlockPosSingle) {
        int start = i * SIZE_INSTANCE_BYTES;
        //uv
        var sprite = particle.sprite;
        buffer.putFloat(start, sprite.getU0());
        buffer.putFloat(start + 4, sprite.getU1());
        buffer.putFloat(start + 4 * 2, sprite.getV0());
        buffer.putFloat(start + 4 * 3, sprite.getV1());
        //color
        buffer.putFloat(start + 4 * 4, particle.rCol);
        buffer.putFloat(start + 4 * 5, particle.gCol);
        buffer.putFloat(start + 4 * 6, particle.bCol);
        buffer.putFloat(start + 4 * 7, particle.alpha);
        //uv2
        float x = Mth.lerp(partialTicks, (float) particle.xo, (float) particle.x);
        float y = Mth.lerp(partialTicks, (float) particle.yo, (float) particle.y);
        float z = Mth.lerp(partialTicks, (float) particle.zo, (float) particle.z);
        simpleBlockPosSingle.set(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        int l;
        if (particle instanceof MadParticle madParticle) {
            Integer l1 = lightCache.get(simpleBlockPosSingle);
            if (l1 != null) {
                l = l1;
            } else {
                l = getLight(particle, new BlockPos(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z));
                lightCache.put(simpleBlockPosSingle.copy(), l);
            }
            l = madParticle.checkEmit(l);
        } else if (!TakeOver.RENDER_CUSTOM_LIGHT.contains(particle.getClass())) {
            Integer l1 = lightCache.get(simpleBlockPosSingle);
            if (l1 != null) {
                l = l1;
            } else {
                l = getLight(particle, new BlockPos(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z));
                lightCache.put(simpleBlockPosSingle.copy(), l);
            }
        } else {
            l = particle.getLightColor(partialTicks);
        }
        buffer.putInt(start + 4 * 8, l & 0x0000_ffff);
        buffer.putInt(start + 4 * 9, l >> 16 & 0x0000_ffff);
        //matrix
        matrix4fSingle.identity().translation(x + camPosCompensate.x, y + camPosCompensate.y, z + camPosCompensate.z)
                .rotate(camera.rotation())
                .scale(particle.getQuadSize(partialTicks));
        var r = Mth.lerp(partialTicks, particle.oRoll, particle.roll);
        if (r != 0) {
            matrix4fSingle.rotateZ(r);
        }
        matrix4fSingle.get(start + 4 * 12, buffer);
    }

    public static void fillVertices(InstancedRenderBufferBuilder bufferBuilder) {
        bufferBuilder.vertex(-1, -1, 0);
        bufferBuilder.uvControl(0, 1, 0, 1).endVertex();

        bufferBuilder.vertex(-1, 1, 0);
        bufferBuilder.uvControl(0, 1, 1, 0).endVertex();

        bufferBuilder.vertex(1, 1, 0);
        bufferBuilder.uvControl(1, 0, 1, 0).endVertex();

        bufferBuilder.vertex(1, -1, 0);
        bufferBuilder.uvControl(1, 0, 0, 1).endVertex();
    }

    public static void prepare(ShaderInstance shader) {
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

        RenderSystem.setupShaderLights(shader);
        shader.apply();
        if (cn.ussshenzhou.madparticle.MadParticle.IS_OPTIFINE_INSTALLED) {
            //TODO if optifine shader using
            GL20C.glUseProgram(shader.getId());
        }
    }

    public static int bindBuffer(ByteBuffer buffer, int id) {
        int bufferId = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, bufferId);
        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, buffer, GL15C.GL_STREAM_DRAW);
        GL30C.glBindVertexArray(id);
        int formerSize = 0;

        GL33C.glEnableVertexAttribArray(INSTANCE_UV_INDEX);
        GL20C.glVertexAttribPointer(INSTANCE_UV_INDEX, 4, GL11C.GL_FLOAT, false, SIZE_INSTANCE_BYTES, formerSize);
        formerSize += 4 * SIZE_FLOAT_OR_INT_BYTES;
        GL33C.glVertexAttribDivisor(INSTANCE_UV_INDEX, 1);

        GL33C.glEnableVertexAttribArray(INSTANCE_COLOR_INDEX);
        GL20C.glVertexAttribPointer(INSTANCE_COLOR_INDEX, 4, GL11C.GL_FLOAT, false, SIZE_INSTANCE_BYTES, formerSize);
        formerSize += 4 * SIZE_FLOAT_OR_INT_BYTES;
        GL33C.glVertexAttribDivisor(INSTANCE_COLOR_INDEX, 1);

        GL33C.glEnableVertexAttribArray(INSTANCE_UV2_INDEX);
        GL30C.glVertexAttribIPointer(INSTANCE_UV2_INDEX, 2, GL11C.GL_INT, SIZE_INSTANCE_BYTES, formerSize);
        formerSize += 4 * SIZE_FLOAT_OR_INT_BYTES;
        GL33C.glVertexAttribDivisor(INSTANCE_UV2_INDEX, 1);

        for (int i = 0; i < 4; i++) {
            GL33C.glEnableVertexAttribArray(INSTANCE_MATRIX_INDEX + i);
            GL20C.glVertexAttribPointer(INSTANCE_MATRIX_INDEX + i, 4, GL11C.GL_FLOAT, false, SIZE_INSTANCE_BYTES, formerSize);
            formerSize += 4 * SIZE_FLOAT_OR_INT_BYTES;
            GL33C.glVertexAttribDivisor(INSTANCE_MATRIX_INDEX + i, 1);
        }

        return bufferId;
    }

    public static void end(ByteBuffer instanceMatrixBuffer, int instanceMatrixBufferId) {
        GL33C.glDisableVertexAttribArray(INSTANCE_UV_INDEX);
        GL33C.glDisableVertexAttribArray(INSTANCE_COLOR_INDEX);
        GL33C.glDisableVertexAttribArray(INSTANCE_UV2_INDEX);
        for (int i = 0; i < 4; i++) {
            GL33C.glDisableVertexAttribArray(INSTANCE_MATRIX_INDEX + i);
        }
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
        GL15C.glDeleteBuffers(instanceMatrixBufferId);
        GL30C.glBindVertexArray(0);
        MemoryUtil.memFree(instanceMatrixBuffer);
    }

    public static class SimpleBlockPos {
        private int x, y, z;

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
