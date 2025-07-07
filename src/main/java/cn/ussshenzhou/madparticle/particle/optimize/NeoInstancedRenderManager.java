package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.util.LightCache;
import cn.ussshenzhou.madparticle.util.SimpleBlockPos;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

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
     * //-----per frame update-----
     * //single float
     * layout (location=0) in vec4 instanceXYZRoll;
     * //-----per tick update-----
     * //half float
     * layout (location=1) in vec4 instanceUV;
     * //half float
     * layout (location=2) in vec4 instanceColor;
     * //half float
     * layout (location=3) in vec2 sizeExtraLight;
     * //(4+4 bits) 1 byte + 3 byte padding
     * layout (location=4) in uint instanceUV2;
     * }</pre>
     */

    private final InstancedArrayBuffer frameVBO = new InstancedArrayBuffer();
    private static final int FRAME_VBO_SIZE = 4 * 4;
    private final InstancedArrayBuffer tickVBO = new InstancedArrayBuffer();
    private static final int TICK_VBO_SIZE = 4 * 2 + 4 * 2 + 2 * 2 + 4;
    private static final LightCache LIGHT_CACHE = new LightCache();
    private static boolean forceMaxLight = false;
    private static final GpuBuffer PROXY_VAO = ModRenderPipelines.INSTANCED_COMMON_DEPTH.getVertexFormat().uploadImmediateVertexBuffer(ByteBuffer.allocateDirect(128));
    private static final GpuBuffer EBO;
    private static final short DEFAULT_EXTRA_LIGHT = Float.floatToFloat16(1f);
    private int amount;
    private boolean tickPassed = true;

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

    public void render(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles) {
        amount = particles.size();
        if (amount == 0) {
            return;
        }
        var mc = Minecraft.getInstance();
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var dynamicTransformsUniform = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                new Matrix4f(),
                0.0F);
        //noinspection DataFlowIssue
        try (var pass = encoder.createRenderPass(
                () -> "MadParticle render",
                mc.getMainRenderTarget().getColorTextureView(),
                OptionalInt.empty(),
                mc.getMainRenderTarget().getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(ModRenderPipelines.INSTANCED_COMMON_DEPTH);
            setUniform(pass, mc, dynamicTransformsUniform);
            setVAO(pass);
            updateFrameVBO(particles, amount);
            updateTickVBO(particles, amount);
            frameVBO.bind();
            tickVBO.bind();
            pass.setIndexBuffer(EBO, VertexFormat.IndexType.INT);
            pass.drawIndexed(0, 0, 6, amount);
        }
        cleanUp();
    }

    private void setVAO(RenderPass pass) {
        pass.setVertexBuffer(0, PROXY_VAO);
        ((GlDevice) RenderSystem.getDevice()).vertexArrayCache().bindVertexArray(ModRenderPipelines.INSTANCED_COMMON_DEPTH.getVertexFormat(), (GlBuffer) PROXY_VAO);
        setVertexAttributeArray();
    }

    private void setUniform(RenderPass pass, Minecraft mc, GpuBufferSlice dynamicTransformsUniform) {
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", dynamicTransformsUniform);
        var camera = mc.gameRenderer.getMainCamera();
        var cameraPos = camera.getPosition();
        var cameraRot = camera.rotation();
        ByteBuffer uniformBuffer = BufferUtils.createByteBuffer((4 + 4) * 4);
        uniformBuffer.putFloat(cameraRot.x);
        uniformBuffer.putFloat(cameraRot.y);
        uniformBuffer.putFloat(cameraRot.z);
        uniformBuffer.putFloat(cameraRot.w);
        uniformBuffer.putFloat((float) cameraPos.x);
        uniformBuffer.putFloat((float) cameraPos.y);
        uniformBuffer.putFloat((float) cameraPos.z);
        uniformBuffer.flip();
        pass.setUniform("CameraCorrection", RenderSystem.getDevice().createBuffer(() -> "MadParticle CameraCorrection Uniform", GpuBuffer.USAGE_UNIFORM, uniformBuffer));
        pass.bindSampler("Sampler0", mc.getTextureManager().getTexture(usingAtlas).getTextureView());
        pass.bindSampler("Sampler2", mc.gameRenderer.lightTexture().getTextureView());
    }

    private void updateFrameVBO(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles, int amount) {
        frameVBO.alloc(FRAME_VBO_SIZE * amount);
        executeUpdate(particles, this::updateFrameVBOInternal, frameVBO);
        frameVBO.update();
    }

    private static final VectorSpecies<Float> SPECIES_4 = FloatVector.SPECIES_128;

    private void updateFrameVBOInternal(ObjectLinkedOpenHashSet<TextureSheetParticle> particles, int index, long frameVBOAddress, float partialTicks) {
        //MemorySegment asSeg = MemorySegment.ofAddress(frameVBOAddress).reinterpret((long) FRAME_VBO_SIZE * amount());
        final var f = new float[4];
        final var fo = new float[4];
        float[] result = new float[4];
        for (TextureSheetParticle particle : particles) {
            //xyz roll
            f[0] = (float) particle.x;
            f[1] = (float) particle.y;
            f[2] = (float) particle.z;
            f[3] = particle.roll;
            fo[0] = (float) particle.xo;
            fo[1] = (float) particle.yo;
            fo[2] = (float) particle.zo;
            fo[3] = particle.oRoll;
            FloatVector vec = FloatVector.fromArray(SPECIES_4, f, 0);
            FloatVector old = FloatVector.fromArray(SPECIES_4, fo, 0);
            var res = old.add(vec.sub(old).mul(partialTicks));
            //JDK-8314791: Vector API MemorySegment stores are slow due to using putIntUnaligned
            //res.intoMemorySegment(asSeg, (long) index * FRAME_VBO_SIZE, ByteOrder.nativeOrder());
            res.intoArray(result, 0);
            long start = frameVBOAddress + (long) index * FRAME_VBO_SIZE;
            MemoryUtil.memPutFloat(start, result[0]);
            MemoryUtil.memPutFloat(start + 4, result[1]);
            MemoryUtil.memPutFloat(start + 8, result[2]);
            MemoryUtil.memPutFloat(start + 12, result[3]);
            index++;
        }
    }

    private void updateTickVBO(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles, int amount) {
        if (!tickPassed) {
            return;
        }
        tickPassed = false;
        tickVBO.alloc(TICK_VBO_SIZE * amount);
        executeUpdate(particles, this::updateTickVBOInternal, tickVBO);
        tickVBO.update();
    }

    private void updateTickVBOInternal(ObjectLinkedOpenHashSet<TextureSheetParticle> particles, int index, long tickVBOAddress, @Deprecated float partialTicks) {
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        for (TextureSheetParticle particle : particles) {
            long start = tickVBOAddress + (long) index * TICK_VBO_SIZE;
            //uv
            MemoryUtil.memPutShort(start, Float.floatToFloat16(particle.getU0()));
            MemoryUtil.memPutShort(start + 2, Float.floatToFloat16(particle.getU1()));
            MemoryUtil.memPutShort(start + 4, Float.floatToFloat16(particle.getV0()));
            MemoryUtil.memPutShort(start + 6, Float.floatToFloat16(particle.getV1()));
            //color
            MemoryUtil.memPutShort(start + 8, Float.floatToFloat16(particle.rCol));
            MemoryUtil.memPutShort(start + 10, Float.floatToFloat16(particle.gCol));
            MemoryUtil.memPutShort(start + 12, Float.floatToFloat16(particle.bCol));
            MemoryUtil.memPutShort(start + 14, Float.floatToFloat16(particle.alpha));
            //size extraLight
            MemoryUtil.memPutShort(start + 16, Float.floatToFloat16(particle.getQuadSize(0.5f)));
            if (particle instanceof MadParticle madParticle) {
                MemoryUtil.memPutShort(start + 18, Float.floatToFloat16(madParticle.getBloomFactor()));
            } else {
                MemoryUtil.memPutShort(start + 18, DEFAULT_EXTRA_LIGHT);
            }
            //uv2
            if (forceMaxLight) {
                MemoryUtil.memPutByte(start + 20, (byte) 0xff);
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
                MemoryUtil.memPutByte(start + 20, l);
            }
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    private void executeUpdate(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles, VboUpdater updater, InstancedArrayBuffer vbo) {
        var partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        CompletableFuture<Void>[] futures = new CompletableFuture[getThreads()];
        int index = 0;
        for (int group = 0; group < futures.length; group++) {
            @SuppressWarnings("rawtypes")
            ObjectLinkedOpenHashSet set = particles.get(group);
            int i = index;
            futures[group] = CompletableFuture.runAsync(
                    () -> updater.update((ObjectLinkedOpenHashSet<TextureSheetParticle>) set, i, vbo.getAddress(), partialTicks),
                    getFixedThreadPool()
            );
            index += set.size();
        }
        CompletableFuture.allOf(futures).join();
    }

    private void setVertexAttributeArray() {
        frameVBO.bind();
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, FRAME_VBO_SIZE, 0);
        glVertexAttribDivisorARB(0, 1);

        tickVBO.bind();
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 0);
        glVertexAttribDivisorARB(1, 1);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 8);
        glVertexAttribDivisorARB(2, 1);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 16);
        glVertexAttribDivisorARB(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribIPointer(4, 1, GL_UNSIGNED_BYTE, TICK_VBO_SIZE, 20);
        glVertexAttribDivisorARB(4, 1);
    }

    private static void cleanUp() {
        GlStateManager._glBindVertexArray(0);
        //for (int i = 0; i < 4; i++) {
        //    glDisableVertexAttribArray(i);
        //}
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDepthMask(true);
    }

    public int getAmount() {
        return amount;
    }

    public void tickPassed() {
        tickPassed = true;
    }

    @FunctionalInterface
    public interface VboUpdater {
        void update(ObjectLinkedOpenHashSet<TextureSheetParticle> particles, int startIndex, long frameVBOAddress, float partialTicks);
    }

}
