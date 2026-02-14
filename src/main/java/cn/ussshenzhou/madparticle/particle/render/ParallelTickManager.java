package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {
    public static ConcurrentHashMap.KeySetView<Object, Boolean> removeCache = ConcurrentHashMap.newKeySet();
    public static Cache<Particle, Object> syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(16384).build();
    public static final Object NULL = new Object();
    private static final LongAdder COUNTER = new LongAdder();
    private static final ThreadLocal<int[]> LOCAL_COUNTER = ThreadLocal.withInitial(() -> new int[1]);
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
        if (((ITickType) particle).getTickType() != TakeOver.TickType.SYNC) {
            asyncTick(particle);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };
    private static final Consumer<Particle> MP_ONLY_TICKER = particle -> {
        if (particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC) {
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
        LOCAL_COUNTER.get()[0]++;
        if (p.removed) {
            removeCache.add(p);
        }
    }

    public static void update(int amount) {
        syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(65536).build();
    }

    private static CompletableFuture<Void> lastTickJob;

    //TODO divide sync caches by takeOverType.
    @SuppressWarnings("unchecked")
    public static void tick(ParticleEngine engine) {
        checkPreviousTickDone(engine);
        NeoInstancedRenderManager.forEach(NeoInstancedRenderManager::preUpdate);
        tickSync();
        removeAndAdd(engine);
        engine.particles.forEach((renderType, particles) -> {
            if (renderType == ModParticleRenderTypes.INSTANCED || renderType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
                NeoInstancedRenderManager.getInstance(renderType).update((MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle>) particles.particles);
            }
        });
        lastTickJob = CompletableFuture.runAsync(() -> {
                    syncTickCache.invalidateAll();
                    removeCache.clear();
                    COUNTER.reset();
                }, MultiThreadHelper.getForkJoinPool())
                .whenCompleteAsync((v, e) -> {
                    var ticker = switch (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking) {
                        case ALL -> ALL_TICKER;
                        case VANILLA -> VANILLA_ONLY_TICKER;
                        case NONE -> MP_ONLY_TICKER;
                    };
                    LOCAL_COUNTER.get()[0] = 0;
                    engine.particles.values().forEach(particleGroup -> {
                        if (particleGroup.particles instanceof MultiThreadedEqualObjectLinkedOpenHashSetQueue<? extends Particle> multiThreadedEqualObjectLinkedOpenHashSetQueue) {
                            multiThreadedEqualObjectLinkedOpenHashSetQueue.forEach(ticker);
                        } else {
                            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit(() -> particleGroup.particles.parallelStream().forEach(ticker));
                            pickAndTick.join();
                        }
                    });
                    COUNTER.add(LOCAL_COUNTER.get()[0]);
                }, MultiThreadHelper.getForkJoinPool())
                .whenCompleteAsync((v, e) -> {
                    if (e != null) {
                        failSafe(engine, e);
                    }
                    engine.particles.forEach((renderType, particleGroup) -> {
                        if (renderType == ModParticleRenderTypes.INSTANCED || renderType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
                            NeoInstancedRenderManager.getInstance(renderType).postUpdate((MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle>) particleGroup.particles);
                        }
                    });
                }, MultiThreadHelper.getForkJoinPool());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "SuspiciousMethodCalls"})
    private static void removeAndAdd(ParticleEngine engine) {
        addCounter.set(engine.particlesToAdd.size());
        removeCounter.set(removeCache.size());
        engine.particles.values().parallelStream().forEach(particleGroup -> particleGroup.particles.removeAll(removeCache));
        engine.particlesToAdd.stream()
                .collect(Collectors.groupingBy(TakeOver::map))
                .forEach((renderType, particles) ->
                        engine.particles.computeIfAbsent(renderType, engine::createParticleGroup).particles.addAll((List) particles));
        engine.particlesToAdd.clear();
    }

    private static void tickSync() {
        syncTickCache.asMap().keySet().forEach(particle -> {
            particle.tick();
            if (!particle.isAlive()) {
                removeCache.add(particle);
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
        removeCache.clear();
        syncTickCache.invalidateAll();
    }

    public static int count() {
        return (int) COUNTER.sum();
    }

    private static int threads() {
        return MultiThreadHelper.getThreads();
    }
}
