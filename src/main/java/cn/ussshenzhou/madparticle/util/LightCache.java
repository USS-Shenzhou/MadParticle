package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author USS_Shenzhou
 */
public class LightCache {
    private static int xRange = 512;
    private static int zRange = 512;
    private static int yRange = 256;

    /**
     * short:
     * <br/>0 0 0 0_0 0 0 0___0 0 0 1_0 0 0 0
     * <br/>____|______|_____________|______|
     * <br/>isCal.ed_value______isCal.ed_value
     * <br/>___odd________________even_____
     */
    private byte[][][] cache = new byte[xRange * 2][zRange * 2][yRange * 2];
    /**
     * Pos in long,packed light in int
     */
    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private final ConcurrentHashMap<Long, Integer> outside = new ConcurrentHashMap<>(32768);

    public LightCache() {
    }

    public void invalidateAll() {
        IntStream.range(0, cache.length).parallel().forEach(i ->
                IntStream.range(0, cache[i].length).forEach(j ->
                        Arrays.fill(cache[i][j], (byte) 0)
                )
        );
        outside.clear();
    }

    public int getOrCompute(int x, int y, int z, Supplier<Integer> computer) {
        if (isInRange(x, y, z)) {
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            int rx = Mth.floor(x - camera.x) + xRange;
            int ry = Mth.floor(y - camera.y) + yRange;
            int rz = Mth.floor(z - camera.z) + zRange;
            byte value = cache[rx][rz][ry];
            if (value >>> 4 == 0) {
                int packetLight = computer.get();
                cache[rx][rz][ry] = (byte) (getMax(packetLight) | 0x10);
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
        return Math.abs(x - camera.x) < xRange && Math.abs(y - camera.y) < yRange && Math.abs(z - camera.z) < zRange;
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
