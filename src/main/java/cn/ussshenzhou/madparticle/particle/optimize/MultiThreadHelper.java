package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author USS_Shenzhou
 */
public class MultiThreadHelper {

    private static int threads = -1;
    private static Executor fixedThreadPool;
    private static ForkJoinPool forkJoinPool;

    static {
        update(ConfigHelper.getConfigRead(MadParticleConfig.class).getBufferFillerThreads());
    }

    public static void update(int threads) {
        boolean initializing = MultiThreadHelper.threads == -1;
        if (threads <= 0 || threads > 256) {
            throw new IllegalArgumentException("The amount of auxiliary threads should between 1 and 256. Correct the config file manually.");
        }
        MultiThreadHelper.threads = threads;
        fixedThreadPool = Executors.newFixedThreadPool(threads, new ThreadFactoryBuilder().setNameFormat("MadParticle-FixedThread-%d").build());
        AtomicInteger index = new AtomicInteger();
        forkJoinPool = new ForkJoinPool(threads, pool -> {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName("MadParticle-JoinPoolThread-" + index.getAndIncrement());
            return thread;
        }, null, false);
        if (!initializing) {
            notifyOthers(threads);
        }
    }

    private static void notifyOthers(int threads) {
        ParallelTickManager.update(threads);
        NeoInstancedRenderManager.forEach(instance -> instance.updateThreads(threads));
    }

    public static int getThreads() {
        return threads;
    }

    public static Executor getFixedThreadPool() {
        return fixedThreadPool;
    }

    public static ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}
