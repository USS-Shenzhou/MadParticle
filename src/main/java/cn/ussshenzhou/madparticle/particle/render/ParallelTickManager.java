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
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {
    public static ConcurrentHashMap.KeySetView<Object, Boolean> removeCache = ConcurrentHashMap.newKeySet();
    public static Cache<Particle, Object> syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(16384).build();
    public static final Object NULL = new Object();
    private static volatile long[] COUNTER = new long[16 * threads()];
    private static volatile int sum = 0;
    public static AtomicInteger addCounter = new AtomicInteger();
    public static AtomicInteger removeCounter = new AtomicInteger();

    static {
        NeoInstancedRenderManager.init();
    }

    private static final ObjIntConsumer<Particle> VANILLA_ONLY_TICKER = (particle, threadId) -> {
        if (getTickType(particle) == TakeOver.TickType.ASYNC) {
            asyncTick(particle, threadId);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };
    private static final ObjIntConsumer<Particle> ALL_TICKER = (particle, threadId) -> {
        if (((ITickType) particle).getTickType() != TakeOver.TickType.SYNC) {
            asyncTick(particle, threadId);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };
    private static final ObjIntConsumer<Particle> MP_ONLY_TICKER = (particle, threadId) -> {
        if (particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC) {
            asyncTick(particle, threadId);
        } else {
            syncTickCache.put(particle, NULL);
        }
    };

    private static TakeOver.TickType getTickType(Particle particle) {
        return ((ITickType) particle).getTickType();
    }

    private static void asyncTick(Particle p, int threadId) {
        p.tick();
        COUNTER[16 * threadId]++;
        if (p.removed) {
            removeCache.add(p);
        }
    }

    public static void update(int threads) {
        syncTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads).initialCapacity(65536).build();
        COUNTER = new long[16 * threads()];
    }

    private static CompletableFuture<Void> lastTickJob;

    //TODO divide sync caches by takeOverType.
    @SuppressWarnings("unchecked")
    public static void tick(ParticleEngine engine) {
        checkPreviousTickDone(engine);
        NeoInstancedRenderManager.forEach(NeoInstancedRenderManager::preUpdate);
        tickSync();
        sum = 0;
        for (int i = 0; i < COUNTER.length / 16; i++) {
            //noinspection NonAtomicOperationOnVolatileField,lossy-conversions
            sum += COUNTER[i * 16];
        }
        removeAndAdd(engine);
        if (engine.particles.values().stream().mapToInt(ParticleGroup::size).sum() == 0) {
            NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED).clear();
            NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED_TERRAIN).clear();
        }
        engine.particles.forEach((renderType, particles) -> {
            if (renderType == ModParticleRenderTypes.INSTANCED || renderType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
                NeoInstancedRenderManager.getInstance(renderType).update((MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle>) particles.particles);
            }
        });
        lastTickJob = CompletableFuture.runAsync(() -> {
                    syncTickCache.invalidateAll();
                    removeCache.clear();
                }, MultiThreadHelper.getForkJoinPool())
                .whenCompleteAsync((v, e) -> {
                    Arrays.fill(COUNTER, 0);
                    var ticker = switch (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking) {
                        case ALL -> ALL_TICKER;
                        case VANILLA -> VANILLA_ONLY_TICKER;
                        case NONE -> MP_ONLY_TICKER;
                    };
                    engine.particles.values().forEach(particleGroup -> {
                        if (particleGroup.particles instanceof MultiThreadedEqualObjectLinkedOpenHashSetQueue<? extends Particle> multiThreadedEqualObjectLinkedOpenHashSetQueue) {
                            multiThreadedEqualObjectLinkedOpenHashSetQueue.forEach(ticker);
                        } else {
                            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit(() -> particleGroup.particles.parallelStream().forEach(p -> ticker.accept(p, 0)));
                            pickAndTick.join();
                        }
                    });
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

    @SuppressWarnings("lossy-conversions")
    public static int count() {
        return sum;
    }

    private static int threads() {
        return MultiThreadHelper.getThreads();
    }
}
