package cn.ussshenzhou.madparticle.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("unchecked")
public class PositionedCache<T> {
    private final Object[] l1;
    private final ByteBuffer modifyFlag;
    private final ConcurrentHashMap<SimpleBlockPos, T> l2 = new ConcurrentHashMap<>();
    @Nullable
    private final Getter<T> getter;
    private final FastMultiReadSingleWriteLock lock = new FastMultiReadSingleWriteLock();
    private int xCenter, yCenter, zCenter;
    private final int xzRange, yRange;

    @FunctionalInterface
    public interface Getter<T> {
        T get(int x, int y, int z);
    }

    public PositionedCache(int xzRange, int yRange, @Nullable Getter<T> getter) {
        l1 = new Object[xzRange * 2 * xzRange * 2 * yRange * 2];
        modifyFlag = MemoryUtil.memCalloc(xzRange * 2 * xzRange * 2 * yRange * 2 / 8);
        NeoForge.EVENT_BUS.register(this);
        this.getter = getter;
        this.xzRange = xzRange;
        this.yRange = yRange;
    }

    @SubscribeEvent
    public void refresh(ClientTickEvent.Post event) {
        //TODO still choose-able
        checkValid();
    }

    public void checkValid() {
        var cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        if (Math.abs(cameraPos.x - xCenter) > 16 || Math.abs(cameraPos.y - yCenter) > 16 || Math.abs(cameraPos.z - zCenter) > 16) {
            invalidateAll();
        }
    }

    public void invalidateAll() {
        var cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        lock.pushWrite();
        MemoryUtil.memSet(modifyFlag, 0);
        xCenter = Mth.floor(cameraPos.x);
        yCenter = Mth.floor(cameraPos.y);
        zCenter = Mth.floor(cameraPos.z);
        lock.popWrite();
        l2.clear();
    }

    private boolean isInRange(int x, int y, int z) {
        return Math.abs(x - xCenter) < xzRange && Math.abs(y - yCenter) < yRange && Math.abs(z - zCenter) < xzRange;
    }

    private int index(int x, int y, int z) {
        return (x * xzRange * 2 + z) * yRange * 2 + y;
    }

    public T getOrCompute(int x, int y, int z) {
        if (isInRange(x, y, z)) {
            int rx = x - xCenter + xzRange;
            int ry = y - yCenter + yRange;
            int rz = z - zCenter + xzRange;
            int index = index(rx, ry, rz);
            T value = (T) l1[index];
            byte mod = modifyFlag.get(index / 8);
            if ((mod >>> (ry % 8) & 1) == 0) {
                //noinspection DataFlowIssue
                value = getter.get(x, y, z);
                if (value == null) {
                    return null;
                }
                l1[index] = value;
                modifyFlag.put(index / 8, (byte) (mod | 1 << ry % 8));
                return value;
            }
            return value;
        }
        var pos = new SimpleBlockPos(x, y, z);
        return l2.computeIfAbsent(pos, _ -> getter.get(x, y, z));
    }

    public T getOrCompute(int x, int y, int z, Supplier<T> getter) {
        if (isInRange(x, y, z)) {
            int rx = x - xCenter + xzRange;
            int ry = y - yCenter + yRange;
            int rz = z - zCenter + xzRange;
            int index = index(rx, ry, rz);
            T value = (T) l1[index];
            byte mod = modifyFlag.get(index / 8);
            if ((mod >>> (ry % 8) & 1) == 0) {
                value = getter.get();
                if (value == null) {
                    return null;
                }
                l1[index] = value;
                modifyFlag.put(index / 8, (byte) (mod | 1 << ry % 8));
                return value;
            }
            return value;
        }
        var pos = new SimpleBlockPos(x, y, z);
        return l2.computeIfAbsent(pos, _ -> getter.get());
    }

    public void pushRead() {
        lock.pushRead();
    }

    public void popRead() {
        lock.popRead();
    }

    public void pushWrite() {
        lock.pushWrite();
    }

    public void popWrite() {
        lock.popWrite();
    }
}
