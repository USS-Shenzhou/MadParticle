package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualLinkedHashSetsQueue;
import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.particle.Particle;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {
    public static Cache<Particle, Object> removeCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(65536).build();
    public static Cache<Particle, Object> syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(65536).build();
    public static final Object NULL = new Object();
    private static final LongAdder COUNTER = new LongAdder();
    private static final Consumer<Particle> VANILLA_ONLY_TICKER = particle -> {
        if (getTickType(particle) == TakeOver.TickType.ASYNC) {
            asyncTick(particle);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };
    private static final Consumer<Particle> ALL_TICKER = particle -> {
        if (getTickType(particle) != TakeOver.TickType.SYNC) {
            asyncTick(particle);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };
    private static CompletableFuture<Void> clearJob = null;

    public static void update(int amount) {
        removeCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(65536).build();
        syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(65536).build();
    }

    public static int count() {
        return (int) COUNTER.sum();
    }

    public static void clearCount() {
        COUNTER.reset();
    }

    private static int threads() {
        return MultiThreadHelper.getThreads();
    }

    public static void tickList(Collection<Particle> particles) {
        COUNTER.reset();
        if (clearJob != null) {
            clearJob.join();
            clearJob = null;
        }
        Consumer<Particle> ticker = ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking == TakeOver.VANILLA ? VANILLA_ONLY_TICKER : ALL_TICKER;
        if (particles instanceof MultiThreadedEqualLinkedHashSetsQueue<Particle> multiThreadedEqualLinkedHashSetsQueue) {
            multiThreadedEqualLinkedHashSetsQueue.forEach(ticker);
        } else {
            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit(() -> particles.parallelStream().forEach(ticker));
            pickAndTick.join();
        }
        syncTickCache.asMap().keySet().forEach(particle -> {
            particle.tick();
            if (!particle.isAlive()) {
                removeCache.put(particle, NULL);
            }
        });
        var r = removeCache.asMap().keySet();
        particles.removeAll(r);
        NeoInstancedRenderManager.forEach(instance -> instance.removeAll(r));
        clearJob = CompletableFuture.runAsync(() -> {
            removeCache.invalidateAll();
            syncTickCache.invalidateAll();
        }, MultiThreadHelper.getForkJoinPool());
    }

    private static void asyncTick(Particle p) {
        p.tick();
        COUNTER.increment();
        if (p.removed) {
            removeCache.put(p, NULL);
        }
    }

    private static TakeOver.TickType getTickType(Particle particle) {
        return ((ITickType) particle).getTickType();
    }
}
