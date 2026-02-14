package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.madparticle.util.LightCache;
import cn.ussshenzhou.madparticle.util.SimpleBlockPos;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.ussshenzhou.madparticle.particle.render.MultiThreadHelper.*;
import static org.lwjgl.opengl.ARBInstancedArrays.*;
import static org.lwjgl.opengl.GL42.*;

/**
 * @author USS_Shenzhou
 */
public class NeoInstancedRenderManager {
    final Identifier usingAtlas;

    public NeoInstancedRenderManager(Identifier usingAtlas) {
        this.usingAtlas = usingAtlas;
    }
    //----------meta manager----------

    /**
     * 0 is for {@link net.minecraft.client.renderer.texture.TextureAtlas#LOCATION_PARTICLES},
     * <br>
     * 1 is for {@link net.minecraft.client.renderer.texture.TextureAtlas#LOCATION_BLOCKS}.
     */
    @SuppressWarnings("deprecation")
    static final NeoInstancedRenderManager[] MANAGER_BY_RENDER_TYPE = new NeoInstancedRenderManager[]{
            new NeoInstancedRenderManager(TextureAtlas.LOCATION_PARTICLES),
            new NeoInstancedRenderManager(TextureAtlas.LOCATION_BLOCKS),
    };

    public static NeoInstancedRenderManager getInstance(ParticleRenderType renderType) {
        if (renderType == ModParticleRenderTypes.INSTANCED) {
            return MANAGER_BY_RENDER_TYPE[0];
        }
        return MANAGER_BY_RENDER_TYPE[1];
    }

    public static Stream<NeoInstancedRenderManager> getAllInstances() {
        return Stream.of(MANAGER_BY_RENDER_TYPE);
    }

    public static void forEach(Consumer<? super NeoInstancedRenderManager> consumer) {
        consumer.accept(MANAGER_BY_RENDER_TYPE[0]);
        consumer.accept(MANAGER_BY_RENDER_TYPE[1]);
    }

    public static void init() {
        LogUtils.getLogger().info("NeoInstancedRenderManager init");
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
    static final int TICK_VBO_SIZE = 8 * 4 + 4 * 2 + 4 * 2 + 2 * 2 + 4;
    static final LightCache LIGHT_CACHE = new LightCache();
    static boolean forceMaxLight = false;
    static final GpuBuffer PROXY_VAO = ModRenderPipelines.INSTANCED_COMMON_DEPTH.getVertexFormat().uploadImmediateVertexBuffer(ByteBuffer.allocateDirect(128));
    static final GpuBuffer EBO;
    static final short DEFAULT_EXTRA_LIGHT = Float.floatToFloat16(1f);

    //-----util-----
    final PersistentMappedArrayBuffer tickVBO = new PersistentMappedArrayBuffer();
    int amount, nextAmount;
    volatile CompletableFuture<Void> updateTickVBOTask = null;
    final MappableRingBuffer cameraCorrectionUbo = new MappableRingBuffer(() -> "MadParticle CameraCorrection Uniform",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            (4 + 4) * 4);
    //-----translucent methods related-----
    private final NormalRenderer normalRenderer = new NormalRenderer(this);
    //-----oit-----
    //final RenderTarget accum = getDevice().createTexture(
    //        "OIT Accum",
    //        GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
    //        TextureFormat.RED8
    //        )

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

        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var cameraUbo = encoder.mapBuffer(cameraCorrectionUbo.currentBuffer(), false, true);
        var dynamicUbo = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
        normalRenderer.doRender(encoder, cameraUbo, dynamicUbo);
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

    void updateTickVBOInternal(ObjectLinkedOpenHashSet<SingleQuadParticle> particles, int index, long tickVBOAddress, @Deprecated float partialTicks) {
        long start = tickVBOAddress + (long) index * TICK_VBO_SIZE;
        for (SingleQuadParticle particle : particles) {
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
            //size extraLight, uv2
            MemoryUtil.memPutShort(start + 48, Float.floatToFloat16(particle.getQuadSize(0.5f)));
            float x = (float) ((particle.x + particle.xo) * 0.5f);
            float y = (float) ((particle.y + particle.yo) * 0.5f);
            float z = (float) ((particle.z + particle.zo) * 0.5f);
            int xi = Mth.floor(x);
            int yi = Mth.floor(y);
            int zi = Mth.floor(z);
            byte l = (byte) 0xff;
            if (particle instanceof MadParticle madParticle) {
                MemoryUtil.memPutShort(start + 50, Float.floatToFloat16(madParticle.getBloomFactor()));
                if (!forceMaxLight) {
                    l = LIGHT_CACHE.getOrCompute(xi, yi, zi, particle);
                    l = madParticle.checkEmit(l);
                }
            } else if (TakeOver.RENDER_CUSTOM_LIGHT.contains(particle.getClass())) {
                MemoryUtil.memPutShort(start + 50, DEFAULT_EXTRA_LIGHT);
                if (!forceMaxLight) {
                    l = LightCache.compressPackedLight(particle.getLightCoords(partialTicks));
                }
            } else {
                MemoryUtil.memPutShort(start + 50, DEFAULT_EXTRA_LIGHT);
                if (!forceMaxLight) {
                    l = LIGHT_CACHE.getOrCompute(xi, yi, zi, particle);
                }
            }
            MemoryUtil.memPutByte(start + 52, l);

            index++;
            start += TICK_VBO_SIZE;
        }
    }

    @SuppressWarnings("unchecked")
    CompletableFuture<Void> executeUpdate(MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> particles, VboUpdater updater, PersistentMappedArrayBuffer vbo) {
        var partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        CompletableFuture<Void>[] futures = new CompletableFuture[particles.threads()];
        int index = 0;
        for (int group = 0; group < futures.length; group++) {
            @SuppressWarnings("rawtypes")
            ObjectLinkedOpenHashSet set = particles.get(group);
            int i = index;
            futures[group] = CompletableFuture.runAsync(
                    () -> updater.update((ObjectLinkedOpenHashSet<SingleQuadParticle>) set, i, vbo.getNext().getAddress(), partialTicks),
                    getFixedThreadPool()
            );
            index += set.size();
        }
        return CompletableFuture.allOf(futures);
    }

    void cleanUp() {
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
        void update(ObjectLinkedOpenHashSet<SingleQuadParticle> particles, int startIndex, long frameVBOAddress, float partialTicks);
    }

    static GlDevice getDevice() {
        var device = RenderSystem.getDevice();
        if (device instanceof ValidationGpuDevice validationGpuDevice) {
            return (GlDevice) validationGpuDevice.getRealDevice();
        } else if (device instanceof GlDevice glDevice) {
            return glDevice;
        }
        throw new IllegalStateException("Unsupported device type: " + device.getClass().getSimpleName());
    }

    static boolean oitOn() {
        return ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod == TranslucentMethod.OIT;
    }

    //----------iris----------
    //FIXME
    void bindIrisFBO() {
        //    if (!cn.ussshenzhou.madparticle.MadParticle.irisOn) {
        //        return;
        //    }
        //    var program = getDevice().getOrCompilePipeline(getRenderType().renderPipeline).program();
        //    try {
        //        var writingToBeforeTranslucentField = program.getClass().getDeclaredField("writingToAfterTranslucent");
        //        writingToBeforeTranslucentField.setAccessible(true);
        //        var writingToBeforeTranslucent = writingToBeforeTranslucentField.get(program);
        //        var bindMethod = writingToBeforeTranslucent.getClass().getDeclaredMethod("bind");
        //        bindMethod.setAccessible(true);
        //        bindMethod.invoke(writingToBeforeTranslucent);
        //        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        //    } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        //        LogUtils.getLogger().error(e.toString());
        //    }
    }

    // RenderType.CompositeRenderType getRenderType() {
    //    return (RenderType.CompositeRenderType) (usingAtlas == TextureAtlas.LOCATION_BLOCKS ? ParticleRenderType.TERRAIN_SHEET.renderType() : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.renderType());
    //}
}
