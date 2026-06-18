package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.enums.LightCacheRefreshInterval;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle;
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
    private final ConcurrentHashMap<SimpleBlockPos, Byte> outside = new ConcurrentHashMap<>();

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

    public byte getOrCompute(int x, int y, int z, SingleQuadParticle particle) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        double cameraX = camera.x;
        double cameraY = camera.y;
        double cameraZ = camera.z;
        if (isInRange(x, y, z, cameraX, cameraY, cameraZ)) {
            int rx = Mth.floor(x - cameraX) + XZ_RANGE;
            int ry = Mth.floor(y - cameraY) + Y_RANGE;
            int rz = Mth.floor(z - cameraZ) + XZ_RANGE;
            byte value = bright[rx][rz][ry];
            int i = rx * XZ_RANGE * 2 * Y_RANGE * 2 / 8 + rz * Y_RANGE * 2 / 8 + ry / 8;
            byte mod = modifyFlag.get(i);
            if ((mod >>> ry % 8 & 1) == 0) {
                bright[rx][rz][ry] = compressPackedLight(getLight(particle, x, y, z));
                modifyFlag.put(i, (byte) (mod | 1 << ry % 8));
                return bright[rx][rz][ry];
            } else {
                return value;
            }
        } else {
            var pos = new SimpleBlockPos(x, y, z);
            var s = outside.get(pos);
            if (s == null) {
                int packetLight = compressPackedLight(getLight(particle, x, y, z));
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

    public static int getLight(SingleQuadParticle particle, int x, int y, int z) {
        var pos = MUTABLE_BLOCK_POS.get().set(x, y, z);
        return particle.level.hasChunkAt(pos) ? LevelRenderer.getLightCoords(particle.level, pos) : 0;
    }

    private byte getMax(int packedLight) {
        int block = packedLight >>> 4 & 0xf;
        int sky = packedLight >>> 20;
        return (byte) (Math.max(block, sky));
    }

    private boolean isInRange(int x, int y, int z, double cameraX, double cameraY, double cameraZ) {
        return Math.abs(x - cameraX) < XZ_RANGE && Math.abs(y - cameraY) < Y_RANGE && Math.abs(z - cameraZ) < XZ_RANGE;
    }
}
