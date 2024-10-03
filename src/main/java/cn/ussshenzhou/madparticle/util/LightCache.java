package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.enums.LightCacheRefreshInterval;
import cn.ussshenzhou.madparticle.particle.optimize.InstancedRenderManager;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author USS_Shenzhou
 */
public class LightCache {
    private static final int XZ_RANGE = ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheXZRange;
    private static final int Y_RANGE = ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheYRange;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile byte[][][] bright = new byte[XZ_RANGE * 2][XZ_RANGE * 2][Y_RANGE * 2];
    private final ByteBuffer modifyFlag = MemoryUtil.memCalloc(XZ_RANGE * 2 * XZ_RANGE * 2 * Y_RANGE * 2 / 8);
    /**
     * Pos in long,packed light in int
     */
    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private final ConcurrentHashMap<Long, Byte> outside = new ConcurrentHashMap<>();

    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);


    public LightCache() {
        NeoForge.EVENT_BUS.register(this);
    }

    static int t = 0;

    @SubscribeEvent
    public void refreshLightCache(ClientTickEvent.Post event) {
        var config = ConfigHelper.getConfigRead(MadParticleConfig.class);
        if (!config.forceMaxLight) {
            var interval = config.lightCacheRefreshInterval;
            if (interval == LightCacheRefreshInterval.FRAME) {
                return;
            }
            t++;
            if (t % interval.getInterval() == 0) {
                this.invalidateAll();
                t = 0;
            }
        }
    }

    @SubscribeEvent
    public void refreshLightCache(RenderFrameEvent.Pre event) {
        var config = ConfigHelper.getConfigRead(MadParticleConfig.class);
        if (!config.forceMaxLight) {
            if (config.lightCacheRefreshInterval == LightCacheRefreshInterval.FRAME) {
                invalidateAll();
            }
        }
    }

    public void invalidateAll() {
        MemoryUtil.memSet(modifyFlag, 0);
        outside.clear();
    }

    public byte getOrCompute(int x, int y, int z, TextureSheetParticle particle, InstancedRenderManager.SimpleBlockPos simpleBlockPos) {
        if (isInRange(x, y, z)) {
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            int rx = Mth.floor(x - camera.x) + XZ_RANGE;
            int ry = Mth.floor(y - camera.y) + Y_RANGE;
            int rz = Mth.floor(z - camera.z) + XZ_RANGE;
            byte value = bright[rx][rz][ry];
            int i = rx * XZ_RANGE * 2 * Y_RANGE * 2 / 8 + rz * Y_RANGE * 2 / 8 + ry / 8;
            byte mod = modifyFlag.get(i);
            if ((mod >>> ry % 8 & 1) == 0) {
                bright[rx][rz][ry] = compressPackedLight(getLight(particle, simpleBlockPos));
                modifyFlag.put(i, (byte) (mod | 1 << ry % 8));
                return bright[rx][rz][ry];
            } else {
                return value;
            }
        } else {
            Long pos = asLong(x, y, z);
            var s = outside.get(pos);
            if (s == null) {
                int packetLight = compressPackedLight(getLight(particle, simpleBlockPos));
                byte r = (byte) ((packetLight >>> 4 & 0xf) | (packetLight >>> 16 & 0xf0));
                outside.put(pos, r);
                return r;
            } else {
                return s;
            }
        }
    }

    public static byte compressPackedLight(int packetLight) {
        return (byte) ((packetLight >>> 4 & 0xf) | (packetLight >>> 16 & 0xf0));
    }

    public static int getLight(TextureSheetParticle particle, InstancedRenderManager.SimpleBlockPos simpleBlockPosSingle) {
        var pos = MUTABLE_BLOCK_POS.get().set(simpleBlockPosSingle.x, simpleBlockPosSingle.y, simpleBlockPosSingle.z);
        return particle.level.hasChunkAt(pos) ? LevelRenderer.getLightColor(particle.level, pos) : 0;
    }

    private byte getMax(int packedLight) {
        int block = packedLight >>> 4 & 0xf;
        int sky = packedLight >>> 20;
        return (byte) (Math.max(block, sky));
    }

    private boolean isInRange(int x, int y, int z) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return Math.abs(x - camera.x) < XZ_RANGE && Math.abs(y - camera.y) < Y_RANGE && Math.abs(z - camera.z) < XZ_RANGE;
    }

    private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    public static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public static long asLong(int x, int y, int z) {
        long i = 0L;
        i |= ((long) (x + 30000001) & PACKED_X_MASK) << X_OFFSET;
        i |= ((long) y & PACKED_Y_MASK);
        return i | ((long) z & PACKED_Z_MASK) << Z_OFFSET;
    }
}
