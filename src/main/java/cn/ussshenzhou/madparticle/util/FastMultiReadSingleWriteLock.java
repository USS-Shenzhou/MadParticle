package cn.ussshenzhou.madparticle.util;

import net.minecraft.util.Mth;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicBoolean;


public class FastMultiReadSingleWriteLock {
    private static final VarHandle COUNTER = MethodHandles.arrayElementVarHandle(long[].class);
    private static final int N = 16384;
    private final long[] readingCounter = new long[16 * N];
    private final AtomicBoolean writing = new AtomicBoolean();

    public FastMultiReadSingleWriteLock() {
    }

    private int i() {
        return ((int) Thread.currentThread().threadId() % N) * 16;
    }

    public void pushRead() {
        int i = i();
        while (true) {
            COUNTER.getAndAdd(readingCounter, i, 1L);
            if (!writing.get()) {
                return;
            }
            COUNTER.getAndAdd(readingCounter, i, -1L);
            while (writing.get()) {
                Thread.onSpinWait();
            }
        }
    }

    public void popRead() {
        COUNTER.getAndAdd(readingCounter, i(), -1L);
    }

    public void pushWrite() {
        while (writing.get() || !writing.compareAndSet(false, true)) {
            Thread.onSpinWait();
        }
        while (someoneElseReading()) {
            Thread.onSpinWait();
        }
    }

    public void popWrite() {
        writing.set(false);
    }

    private boolean someoneElseReading() {
        int i = (int) Thread.currentThread().threadId() * 16;
        for (int i1 = 0; i1 < readingCounter.length; i1 += 16) {
            if (i1 == i) {
                continue;
            }
            if ((long) COUNTER.getVolatile(readingCounter, i1) != 0) {
                return true;
            }
        }
        return false;
    }
}
