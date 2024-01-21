package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

/**
 * @author USS_Shenzhou
 */
public class ParallelTickManager {

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(threads());
    public static Cache<Particle, Object> removeCache = CacheBuilder.newBuilder().concurrencyLevel(threads()).initialCapacity(1024).build();
    public static Object NULL = new Object();

    public static void setThreads(int amount) {
        removeCache = CacheBuilder.newBuilder().concurrencyLevel(amount).initialCapacity(1024).build();
        forkJoinPool = new ForkJoinPool(amount);
    }

    private static int threads() {
        return InstancedRenderManager.getThreads();
    }

    public static void tickList(Collection<Particle> particles) {
        boolean vanillaOnly = ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking == TakeOver.VANILLA;
        var a = forkJoinPool.submit(() -> {
            var stream = particles.parallelStream().filter(particle -> particle instanceof TextureSheetParticle);
            if (vanillaOnly) {
                stream = stream.filter(particle -> TakeOver.ASYNC_TICK_VANILLA_AND_MADPARTICLE.contains(particle.getClass()));
            } else {
                stream = stream.filter(particle -> !TakeOver.SYNC_TICK_VANILLA_AND_MADPARTICLE.contains(particle.getClass()));
            }
            stream.forEach(particle -> {
                particle.tick();
                if (!particle.isAlive()) {
                    removeCache.put(particle, NULL);
                }
            });
        });

        //tick other mods' particles if needed
        var stream = particles.parallelStream();
        if (vanillaOnly) {
            stream = stream.filter(particle -> !TakeOver.ASYNC_TICK_VANILLA_AND_MADPARTICLE.contains(particle.getClass()));
        } else {
            stream = stream.filter(particle -> TakeOver.SYNC_TICK_VANILLA_AND_MADPARTICLE.contains(particle.getClass()));
        }
        stream.sequential().forEach(particle -> {
            particle.tick();
            if (!particle.isAlive()) {
                removeCache.put(particle, NULL);
            }
        });
        a.join();
        var r = removeCache.asMap().keySet();
        particles.removeAll(r);
        InstancedRenderManager.removeAll(r);
        removeCache.invalidateAll();
    }
}