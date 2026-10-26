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
import java.util.function.Predicate;
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
                    final Predicate<Particle> condition = switch (takeoverTicking) {
                        case ALL -> ParallelTickManager::allTakeoverTick;
                        case VANILLA -> ParallelTickManager::vanillaTakeoverTick;
                        case NONE -> ParallelTickManager::noneTakeoverTick;
                    };
                    engine.particles.forEach((particleRenderType, particleGroup) -> {
                        if (particleGroup.particles instanceof MultiThreadedObjectArrayListQueue<? extends Particle> multiThreadedObjectArrayListQueue) {
                            final var neoInstancedRenderManager = NeoInstancedRenderManager.getInstance(particleRenderType);
                            var particles = (MultiThreadedObjectArrayListQueue<SingleQuadParticle>) multiThreadedObjectArrayListQueue;
                            var removed = particles.forEachAllowRemove(((particle, threadId, overallIndex) -> condition.test(particle) ? tickAndWriteVBO(particle, threadId, overallIndex, neoInstancedRenderManager) : delayedTickAndWriteVBO(particle, overallIndex, neoInstancedRenderManager)));
                            REMOVE_COUNTER.addAndGet(removed);
                        } else if (particleGroup.particles instanceof MultiThreadedEqualObjectOpenHashSetQueue<? extends Particle> multiThreadedEqualObjectOpenHashSetQueue) {
                            var particles = (MultiThreadedEqualObjectOpenHashSetQueue<SingleQuadParticle>) multiThreadedEqualObjectOpenHashSetQueue;
                            particles.forEach((particle, threadId)-> {
                                if (condition.test(particle)) {
                                    ASYNC_TICK_COUNTER[16 * threadId]++;
                                    tick(particleRenderType, particle);
                                } else {
                                    syncTickRemoveCache.put(particle, NULL);
                                }
                            });
                        } else {
                            ForkJoinTask<?> pickAndTick = MultiThreadHelper.getForkJoinPool().submit((() -> particleGroup.particles.parallelStream().forEach(particle -> {
                                if (condition.test(particle)) {
                                    OTHER_ASYNC_TICK_COUNTER.getAndIncrement();
                                    tick(particleRenderType, particle);
                                } else {
                                    syncTickRemoveCache.put(particle, NULL);
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
                    asyncTimeMs = (System.nanoTime() - asyncStartNs) / 1000_000d;
                }, MultiThreadHelper.getFixedThreadPool());
    }

    private static void tick(ParticleRenderType particleRenderType, Particle particle) {
        particle.tick();
        if (particle.removed) {
            removeCache.get(particleRenderType).add(particle);
        }
    }

    private static boolean delayedTickAndWriteVBO(SingleQuadParticle particle, int overallIndex, NeoInstancedRenderManager neoInstancedRenderManager) {
        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
        if (particle.removed) {
            return true;
        }
        syncJustTickCache.put(particle, NULL);
        return false;
    }

    private static boolean tickAndWriteVBO(SingleQuadParticle particle, int threadId, int overallIndex, NeoInstancedRenderManager neoInstancedRenderManager) {
        ASYNC_TICK_COUNTER[16 * threadId]++;
        particle.tick();
        neoInstancedRenderManager.updateTickVBOSingleParticle(particle, overallIndex);
        return particle.removed;
    }

    private static boolean noneTakeoverTick(Particle particle) {
        return particle instanceof MadParticle && getTickType(particle) == TakeOver.TickType.ASYNC;
    }

    private static boolean vanillaTakeoverTick(Particle particle) {
        return getTickType(particle) == TakeOver.TickType.ASYNC;
    }

    private static boolean allTakeoverTick(Particle particle) {
        return getTickType(particle) != TakeOver.TickType.SYNC;
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
