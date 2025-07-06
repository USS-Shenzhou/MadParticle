package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualLinkedHashSetsQueue;
import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.EvictingQueue;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {
    public static Cache<Particle, Object> removeCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(65536).build();
    public static Cache<Particle, Object> syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(65536).build();
    public static final Object NULL = new Object();
    private static final LongAdder COUNTER = new LongAdder();
    public static AtomicInteger addCounter = new AtomicInteger();
    public static AtomicInteger removeCounter = new AtomicInteger();

    static {
        NeoInstancedRenderManager.init();
    }

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

    private static TakeOver.TickType getTickType(Particle particle) {
        return ((ITickType) particle).getTickType();
    }

    private static void asyncTick(Particle p) {
        p.tick();
        COUNTER.increment();
        if (p.removed) {
            removeCache.put(p, NULL);
        }
    }

    public static void update(int amount) {
        removeCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(65536).build();
        syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(65536).build();
    }

    private static CompletableFuture<Void> lastTickJob;

    //TODO divide sync caches by renderType.
    public static void tick(ParticleEngine engine) {
        checkPreviousTickDone(engine);
        tickSync();
        removeAndAdd(engine);
        lastTickJob = CompletableFuture.runAsync(() -> {
                    syncTickCache.invalidateAll();
                    removeCache.invalidateAll();
                }, MultiThreadHelper.getForkJoinPool())
                .whenCompleteAsync((v, e) -> {
                    COUNTER.reset();
                    Consumer<Particle> ticker = ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking == TakeOver.VANILLA ? VANILLA_ONLY_TICKER : ALL_TICKER;
                    engine.particles.values().forEach(particles -> {
                        if (particles instanceof MultiThreadedEqualLinkedHashSetsQueue<Particle> multiThreadedEqualLinkedHashSetsQueue) {
                            multiThreadedEqualLinkedHashSetsQueue.forEach(ticker);
                        } else {
                            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit(() -> particles.parallelStream().forEach(ticker));
                            pickAndTick.join();
                        }
                    });
                }, MultiThreadHelper.getForkJoinPool())
                .whenCompleteAsync((v, e) -> {
                    if (e != null) {
                        failSafe(engine, e);
                    }
                }, MultiThreadHelper.getForkJoinPool());
    }

    private static void removeAndAdd(ParticleEngine engine) {
        addCounter.set(engine.particlesToAdd.size());
        removeCounter.set((int) removeCache.size());
        engine.particles.values().parallelStream().forEach(particles -> particles.removeAll(removeCache.asMap().keySet()));
        engine.particlesToAdd.stream()
                .collect(Collectors.groupingBy(TakeOver::map))
                .forEach((renderType, particles) ->
                        engine.particles.computeIfAbsent(renderType, t ->
                                new MultiThreadedEqualLinkedHashSetsQueue<>(16384, ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue)
                        ).addAll(particles));
        NeoInstancedRenderManager.forEach(NeoInstancedRenderManager::tickPassed);
        engine.particlesToAdd.clear();
    }

    private static void tickSync() {
        syncTickCache.asMap().keySet().forEach(particle -> {
            particle.tick();
            if (!particle.isAlive()) {
                removeCache.put(particle, NULL);
            }
        });
    }

    private static void checkPreviousTickDone(ParticleEngine engine) {
        if (lastTickJob == null) {
            return;
        }
        try {
            lastTickJob.join();
        } catch (Exception e) {
            failSafe(engine, e);
        }
    }

    private static void failSafe(ParticleEngine engine, Throwable e) {
        LogUtils.getLogger().error(e.getMessage());
        engine.particles.clear();
        engine.particlesToAdd.clear();
        removeCache.invalidateAll();
        syncTickCache.invalidateAll();
    }

    public static int count() {
        return (int) COUNTER.sum();
    }

    private static int threads() {
        return MultiThreadHelper.getThreads();
    }
}
