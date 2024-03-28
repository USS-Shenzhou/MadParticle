package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class LightCache {
    private static final int XZ_RANGE = ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheXZRange;
    private static final int Y_RANGE = ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheYRange;

    /**
     * byte:
     * <br/>0 0 0 0_0 0 0 0
     * <br/>______|_____|__
     * <br/>isCal.ed__value
     */
    @SuppressWarnings("FieldMayBeFinal")
    private volatile byte[][][] bright = new byte[XZ_RANGE * 2][XZ_RANGE * 2][Y_RANGE];
    private final ByteBuffer modify = MemoryUtil.memCalloc(XZ_RANGE * 2 * XZ_RANGE * 2 * Y_RANGE * 2 / 8);
    /**
     * Pos in long,packed light in int
     */
    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private final ConcurrentHashMap<Long, Integer> outside = new ConcurrentHashMap<>(32768);


    public LightCache() {
    }

    public void invalidateAll() {
        MemoryUtil.memSet(modify, 0);
        outside.clear();
    }

    public int getOrCompute(int x, int y, int z, Supplier<Integer> computer) {
        if (isInRange(x, y, z)) {
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            int rx = Mth.floor(x - camera.x) + XZ_RANGE;
            int ry = Mth.floor(y - camera.y) + Y_RANGE;
            int rz = Mth.floor(z - camera.z) + XZ_RANGE;
            byte value = bright[rx][rz][ry / 2];
            int i = rx * XZ_RANGE * 2 * Y_RANGE * 2 / 8 + rz * Y_RANGE * 2 / 8 + ry / 8;
            byte mod = modify.get(i);
            if ((mod >>> ry % 8 & 1) == 0) {
                int packetLight = computer.get();
                bright[rx][rz][ry / 2] = (byte) (getMax(packetLight) << ry % 2);
                modify.put(i, (byte) (mod | 1 << ry % 8));
                return packetLight;
            } else {
                return value << 4 & 0xf0;
            }
        } else {
            Long pos = asLong(x, y, z);
            var s = outside.get(pos);
            if (s == null) {
                var r = computer.get();
                outside.put(pos, r);
                return r;
            } else {
                return s;
            }
        }
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
