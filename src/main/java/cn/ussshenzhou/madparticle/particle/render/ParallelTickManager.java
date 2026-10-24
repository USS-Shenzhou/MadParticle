package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.util.MultiThreadedEqualObjectOpenHashSetQueue;
import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.util.MultiThreadedObjectArrayListQueue;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {
    public static HashMap<ParticleRenderType, ConcurrentHashMap.KeySetView<Object, Boolean>> removeCache = new HashMap<>();
    public static Cache<Particle, Object> syncJustTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(16384).build();
    public static Cache<Particle, Object> syncTickRemoveCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(16384).build();
    public static final Object NULL = new Object();
    private static volatile long[] ASYNC_TICK_COUNTER = new long[16 * threads()];
    private static final AtomicInteger OTHER_ASYNC_TICK_COUNTER = new AtomicInteger();
    private static volatile int asyncTickSum = 0;
    public static final AtomicInteger ADD_COUNTER = new AtomicInteger();
    public static final AtomicInteger REMOVE_COUNTER = new AtomicInteger();
    public static volatile double asyncTimeMs;
    public static long asyncStartNs;
    public static volatile double syncTimeMs;
    public static long syncStartNs;

    static {
        NeoInstancedRenderManager.init();
    }

    private static TakeOver.TickType getTickType(Particle particle) {
        return ((ITickType) particle).getTickType();
    }

    public static void update(int threads) {
        syncJustTickCache = CacheBuilder.newBuilder().concurrencyLevel(threads).initialCapacity(16384).build();
        syncTickRemoveCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(16384).build();
        ASYNC_TICK_COUNTER = new long[16 * threads()];
    }

    private static CompletableFuture<Void> lastTickJob;

    //TODO divide sync caches by takeOverType.
    @SuppressWarnings("unchecked")
    public static void tick(ParticleEngine engine) {
        checkPreviousTickDone(engine);
        syncStartNs = System.nanoTime();
        NeoInstancedRenderManager.forEach(NeoInstancedRenderManager::finish);
        tickSync();
        asyncTickSum = OTHER_ASYNC_TICK_COUNTER.get();
        for (int i = 0; i < ASYNC_TICK_COUNTER.length / 16; i++) {
            //noinspection NonAtomicOperationOnVolatileField,lossy-conversions
            asyncTickSum += ASYNC_TICK_COUNTER[i * 16];
        }
        removeAndAdd(engine);
        engine.particles.forEach((renderType, _) -> removeCache.computeIfAbsent(renderType, _ -> ConcurrentHashMap.newKeySet()));
        Stream.of(
                ModParticleRenderTypes.INSTANCED,
                ModParticleRenderTypes.INSTANCED_TERRAIN,
                ModParticleRenderTypes.INSTANCED_ITEM
        ).forEach(renderType ->
                NeoInstancedRenderManager.getInstance(renderType)
                        .prepare(engine.particles.containsKey(renderType) ? (Collection<Particle>) engine.particles.get(renderType).particles : List.of()));
        syncTimeMs = (System.nanoTime() - syncStartNs) / 1000_000d;
        lastTickJob = CompletableFuture.runAsync(() -> {
                    asyncStartNs = System.nanoTime();
                    syncJustTickCache.invalidateAll();
                    syncTickRemoveCache.invalidateAll();
                    removeCache.values().forEach(particles -> particles.clear());
                    Arrays.fill(ASYNC_TICK_COUNTER, 0);
                    OTHER_ASYNC_TICK_COUNTER.set(0);
                    REMOVE_COUNTER.set(0);
                }, MultiThreadHelper.getFixedThreadPool())
                .whenCompleteAsync((v, e) -> {
                    final var takeoverTicking = ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking;
                    engine.particles.forEach((particleRenderType, particleGroup) -> {
                        if (particleGroup.particles instanceof MultiThreadedObjectArrayListQueue<? extends Particle> multiThreadedObjectArrayListQueue) {
                            final var neoInstancedRenderManager = NeoInstancedRenderManager.getInstance(particleRenderType);
                            var particles = (MultiThreadedObjectArrayListQueue<SingleQuadParticle>) multiThreadedObjectArrayListQueue;
                            var removed = switch (takeoverTicking) {
                                case ALL -> particles.forEachAllowRemove(((particle, threadId, overallIndex) -> {
                                    if (((ITickType) particle).getTickType() != TakeOver.TickType.SYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        return particle.removed;
                                    } else {
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        if (particle.removed) {
                                            return true;
                                        }
                                        syncJustTickCache.put(particle, NULL);
                                        return false;
                                    }
                                }));
                                case VANILLA -> particles.forEachAllowRemove(((particle, threadId, overallIndex) -> {
                                    if (getTickType(particle) == TakeOver.TickType.ASYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        return particle.removed;
                                    } else {
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        if (particle.removed) {
                                            return true;
                                        }
                                        syncJustTickCache.put(particle, NULL);
                                        return false;
                                    }
                                }));
                                case NONE -> particles.forEachAllowRemove(((particle, threadId, overallIndex) -> {
                                    if (particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        return particle.removed;
                                    } else {
                                        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
                                        if (particle.removed) {
                                            return true;
                                        }
                                        syncJustTickCache.put(particle, NULL);
                                        return false;
                                    }
                                }));
                            };
                            REMOVE_COUNTER.addAndGet(removed);
                        } else if (particleGroup.particles instanceof MultiThreadedEqualObjectOpenHashSetQueue<? extends Particle> multiThreadedEqualObjectOpenHashSetQueue) {
                            switch (takeoverTicking) {
                                case ALL -> multiThreadedEqualObjectOpenHashSetQueue.forEach(((particle, threadId) -> {
                                    if (((ITickType) particle).getTickType() != TakeOver.TickType.SYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        if (particle.removed) {
                                            removeCache.get(particleRenderType).add(particle);
                                        }
                                    } else {
                                        syncTickRemoveCache.put(particle, NULL);
                                    }
                                }));
                                case VANILLA -> multiThreadedEqualObjectOpenHashSetQueue.forEach(((particle, threadId) -> {
                                    if (getTickType(particle) == TakeOver.TickType.ASYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        if (particle.removed) {
                                            removeCache.get(particleRenderType).add(particle);
                                        }
                                    } else {
                                        syncTickRemoveCache.put(particle, NULL);
                                    }
                                }));
                                case NONE -> multiThreadedEqualObjectOpenHashSetQueue.forEach(((particle, threadId) -> {
                                    if (particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC) {
                                        ASYNC_TICK_COUNTER[16 * threadId]++;
                                        particle.tick();
                                        if (particle.removed) {
                                            removeCache.get(particleRenderType).add(particle);
                                        }
                                    } else {
                                        syncTickRemoveCache.put(particle, NULL);
                                    }
                                }));
                            }
                        } else {
                            //ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit(() -> particleGroup.particles.parallelStream().forEach(p -> ticker.accept(p, 0)));
                            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit((() -> particleGroup.particles.parallelStream().forEach(particle -> {
                                switch (takeoverTicking) {
                                    case ALL -> {
                                        if (((ITickType) particle).getTickType() != TakeOver.TickType.SYNC) {
                                            OTHER_ASYNC_TICK_COUNTER.getAndIncrement();
                                            particle.tick();
                                            if (particle.removed) {
                                                removeCache.get(particleRenderType).add(particle);
                                            }
                                        } else {
                                            syncTickRemoveCache.put(particle, NULL);
                                        }
                                    }
                                    case VANILLA -> {
                                        if (getTickType(particle) == TakeOver.TickType.ASYNC) {
                                            OTHER_ASYNC_TICK_COUNTER.getAndIncrement();
                                            particle.tick();
                                            if (particle.removed) {
                                                removeCache.get(particleRenderType).add(particle);
                                            }
                                        } else {
                                            syncTickRemoveCache.put(particle, NULL);
                                        }
                                    }
                                    case NONE -> {
                                        if (particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC) {
                                            OTHER_ASYNC_TICK_COUNTER.getAndIncrement();
                                            particle.tick();
                                            if (particle.removed) {
                                                removeCache.get(particleRenderType).add(particle);
                                            }
                                        } else {
                                            syncTickRemoveCache.put(particle, NULL);
                                        }
                                    }
                                }
                            })));
                            pickAndTick.join();
                        }
                    });
                }, MultiThreadHelper.getFixedThreadPool())
                .whenCompleteAsync((v, e) -> {
                    if (e != null) {
                        failSafe(engine, e);
                    }
                    //var postUpdates = new ArrayList<CompletableFuture<Void>>();
                    //engine.particles.forEach((renderType, particleGroup) -> {
                    //    if (MultiThreadedXQueueHelper.instanced(renderType)) {
                    //        postUpdates.add(NeoInstancedRenderManager.getInstance(renderType).postUpdate((MultiThreadedEqualObjectOpenHashSetQueue<Particle>) particleGroup.particles));
                    //    }
                    //});
                    //CompletableFuture.allOf(postUpdates.toArray(CompletableFuture[]::new)).whenComplete((_, _) -> asyncTimeMs = (System.nanoTime() - asyncStartNs) / 1000_000d);
                    asyncTimeMs = (System.nanoTime() - asyncStartNs) / 1000_000d;
                }, MultiThreadHelper.getFixedThreadPool());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "SuspiciousMethodCalls"})
    private static void removeAndAdd(ParticleEngine engine) {
        ADD_COUNTER.set(engine.particlesToAdd.size());
        REMOVE_COUNTER.addAndGet(removeCache.values().stream().mapToInt(k -> k.size()).sum());
        engine.particles.forEach(((particleRenderType, particleGroup) -> particleGroup.particles.removeAll(removeCache.get(particleRenderType))));
        engine.particlesToAdd.stream()
                .collect(Collectors.groupingBy(TakeOver::map))
                .forEach((renderType, particles) ->
                        engine.particles.computeIfAbsent(renderType, engine::createParticleGroup).particles.addAll((List) particles));
        engine.particlesToAdd.clear();
    }

    private static void tickSync() {
        syncTickRemoveCache.asMap().keySet().forEach(particle -> {
            particle.tick();
            if (particle.removed) {
                removeCache.get(particle.getGroup()).add(particle);
            }
        });
        syncJustTickCache.asMap().keySet().forEach(Particle::tick);
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
        LogUtils.getLogger().error(e.getMessage(), e);
        engine.particles.clear();
        engine.particlesToAdd.clear();
        removeCache.clear();
        syncJustTickCache.invalidateAll();
        syncTickRemoveCache.invalidateAll();
    }

    public static int count() {
        return asyncTickSum;
    }

    private static int threads() {
        return MultiThreadHelper.getThreads();
    }
}
