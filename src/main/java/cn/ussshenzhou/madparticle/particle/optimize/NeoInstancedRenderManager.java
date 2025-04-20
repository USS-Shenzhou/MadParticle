package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.util.LightCache;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.logging.LogUtils;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import net.minecraft.Util;
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
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.ussshenzhou.madparticle.particle.optimize.MultiThreadHelper.*;
import static org.lwjgl.opengl.GL42.*;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class NeoInstancedRenderManager {
    private final ResourceLocation usingAtlas;

    public NeoInstancedRenderManager(ResourceLocation usingAtlas) {
        this.usingAtlas = usingAtlas;
        initRender();
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

    //----------particle----------
    @SuppressWarnings("unchecked")
    private LinkedHashSet<TextureSheetParticle>[] particles = Stream.generate(() -> Sets.newLinkedHashSetWithExpectedSize(32768)).limit(getThreads()).toArray(LinkedHashSet[]::new);

    @SuppressWarnings("unchecked")
    public void updateThreads(int threads) {
        var p = particles;
        particles = Stream.generate(() -> Sets.newLinkedHashSetWithExpectedSize(32768)).limit(threads).toArray(LinkedHashSet[]::new);
        Arrays.stream(p).forEach(set -> set.forEach(this::add));
    }

    private HashSet<TextureSheetParticle> findSmallestSet() {
        HashSet<TextureSheetParticle> r = particles[0];
        int minSize = r.size();
        for (int i = 1; i < getThreads(); i++) {
            if (particles[i].size() < minSize) {
                r = particles[i];
                minSize = r.size();
            }
        }
        return r;
    }

    public void add(TextureSheetParticle particle) {
        findSmallestSet().add(particle);
    }

    public void reload(Collection<Particle> particles) {
        clear();
        particles.forEach(p -> add((TextureSheetParticle) p));
    }

    public void remove(TextureSheetParticle particle) {
        for (int i = 0; i < getThreads(); i++) {
            if (particles[i].remove(particle)) {
                break;
            }
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void removeAll(Collection<Particle> particle) {
        CompletableFuture<?>[] futures = new CompletableFuture[getThreads()];
        for (int i = 0; i < getThreads(); i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> particles[finalI].removeAll(particle),
                    MultiThreadHelper.getFixedThreadPool()
            );
        }
        CompletableFuture.allOf(futures).join();
    }

    public void clear() {
        Arrays.stream(particles).forEach(HashSet::clear);
    }

    public int amount() {
        int size = 0;
        for (int i = 0; i < getThreads(); i++) {
            size += particles[i].size();
        }
        return size;
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
    private boolean tickPassed = true;
    private static final LightCache LIGHT_CACHE = new LightCache();
    private static boolean forceMaxLight = false;
    private final int VAO = GlStateManager._glGenVertexArrays();

    private void initRender() {
        GlStateManager._glBindVertexArray(VAO);
        setVertexAttributeArray();
        GlStateManager._glBindVertexArray(0);
    }

    static {
        NeoForge.EVENT_BUS.addListener(NeoInstancedRenderManager::checkForceMaxLight);
    }

    @SubscribeEvent
    public static void checkForceMaxLight(ClientTickEvent.Pre event) {
        forceMaxLight = ConfigHelper.getConfigRead(MadParticleConfig.class).forceMaxLight;
    }

    public void render() {
        int amount = amount();
        if (amount == 0) {
            return;
        }
        var mc = Minecraft.getInstance();
        var encoder = InstancedGlCommandEncoder.getInstance();
        InstancedGlCommandEncoder.setInstanceToDraw(amount);
        //noinspection DataFlowIssue
        try (var pass = encoder.createRenderPass(
                mc.getMainRenderTarget().getColorTexture(),
                OptionalInt.empty(),
                mc.getMainRenderTarget().getDepthTexture(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(ModRenderPipelines.INSTANCED_COMMON_DEPTH);
            var camera = mc.gameRenderer.getMainCamera();
            var cameraPos = camera.getPosition();
            pass.setUniform("CamXYZ", (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
            var cameraRot = camera.rotation();
            pass.setUniform("CamQuat", cameraRot.x, cameraRot.y, cameraRot.z, cameraRot.w);
            pass.bindSampler("Sampler0", mc.getTextureManager().getTexture(usingAtlas).getTexture());
            pass.bindSampler("Sampler2", mc.gameRenderer.lightTexture().getTarget());
            updateFrameVBO(amount);
            updateTickVBO(amount);
            GlStateManager._glBindVertexArray(VAO);
            encoder.trySetup((GlRenderPass) pass);
            frameVBO.bind();
            tickVBO.bind();
            pass.draw(0, 4);
        }
        cleanUp();
    }

    public void tickPassed() {
        tickPassed = true;
    }

    private void updateFrameVBO(int amount) {
        frameVBO.alloc(FRAME_VBO_SIZE * amount);
        executeUpdate(this::updateFrameVBOInternal, frameVBO);
        frameVBO.update();
    }

    private static final VectorSpecies<Float> SPECIES_4 = FloatVector.SPECIES_128;

    private void updateFrameVBOInternal(LinkedHashSet<TextureSheetParticle> particles, int index, long frameVBOAddress, float partialTicks) {
        MemorySegment asSeg = MemorySegment.ofAddress(frameVBOAddress).reinterpret(FRAME_VBO_SIZE * amount());

        long timeA = Util.getNanos();
        for (TextureSheetParticle particle : particles) {
            //long start = frameVBOAddress + (long) index * FRAME_VBO_SIZE;
            ////xyz roll
            //float x = (float) (particle.xo + partialTicks * (particle.x - particle.xo));
            //float y = (float) (particle.yo + partialTicks * (particle.y - particle.yo));
            //float z = (float) (particle.zo + partialTicks * (particle.z - particle.zo));
            //float roll = particle.oRoll + partialTicks * (particle.roll - particle.oRoll);
            //MemoryUtil.memPutFloat(start, x);
            //MemoryUtil.memPutFloat(start + 4, y);
            //MemoryUtil.memPutFloat(start + 8, z);
            //MemoryUtil.memPutFloat(start + 12, roll);
            var f = new float[]{(float) particle.x, (float) particle.y, (float) particle.z, particle.roll};
            FloatVector vec = FloatVector.fromArray(SPECIES_4, f, 0);
            var fo = new float[]{(float) particle.xo, (float) particle.yo, (float) particle.zo, particle.oRoll};
            FloatVector old = FloatVector.fromArray(SPECIES_4, fo, 0);
            var res = old.add(vec.sub(old).mul(partialTicks));
            res.intoMemorySegment(asSeg, (long) index * FRAME_VBO_SIZE, ByteOrder.nativeOrder());
            index++;
        }
    }

    private void updateTickVBO(int amount) {
        if (!tickPassed) {
            return;
        }
        tickPassed = false;
        tickVBO.alloc(TICK_VBO_SIZE * amount);
        executeUpdate(this::updateTickVBOInternal, tickVBO);
        tickVBO.update();
    }

    private void updateTickVBOInternal(LinkedHashSet<TextureSheetParticle> particles, int index, long tickVBOAddress, @Deprecated float partialTicks) {
        var simpleBlockPosSingle = new SimpleBlockPos(0, 0, 0);
        for (TextureSheetParticle particle : particles) {
            long start = tickVBOAddress + (long) index * TICK_VBO_SIZE;
            //uv
            var sprite = particle.sprite;
            MemoryUtil.memPutShort(start, Float.floatToFloat16(sprite.u0));
            MemoryUtil.memPutShort(start + 2, Float.floatToFloat16(sprite.u1));
            MemoryUtil.memPutShort(start + 4, Float.floatToFloat16(sprite.v0));
            MemoryUtil.memPutShort(start + 6, Float.floatToFloat16(sprite.v1));
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
                MemoryUtil.memPutShort(start + 18, (short) 1);
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
    private void executeUpdate(VboUpdater updater, InstancedArrayBuffer vbo) {
        var partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        CompletableFuture<Void>[] futures = new CompletableFuture[getThreads()];
        int index = 0;
        for (int group = 0; group < futures.length; group++) {
            var set = particles[group];
            int i = index;
            futures[group] = CompletableFuture.runAsync(
                    () -> updater.update(set, i, vbo.getAddress(), partialTicks),
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
        glVertexAttribDivisor(0, 1);

        tickVBO.bind();
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 0);
        glVertexAttribDivisor(1, 1);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 8);
        glVertexAttribDivisor(2, 1);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 16);
        glVertexAttribDivisor(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribIPointer(4, 1, GL_UNSIGNED_BYTE, TICK_VBO_SIZE, 20);
        glVertexAttribDivisor(4, 1);
    }

    private static void cleanUp() {
        GlStateManager._glBindVertexArray(0);
        //for (int i = 0; i < 4; i++) {
        //    glDisableVertexAttribArray(i);
        //}
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDepthMask(true);
    }

    @FunctionalInterface
    public interface VboUpdater {
        void update(LinkedHashSet<TextureSheetParticle> particles, int startIndex, long frameVBOAddress, float partialTicks);
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
