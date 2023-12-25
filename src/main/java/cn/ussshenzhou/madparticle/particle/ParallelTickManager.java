package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.particle.Particle;

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
        forkJoinPool.submit(() -> {
            var stream = particles.parallelStream();
            if (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking == TakeOver.VANILLA) {
                stream = stream.filter(particle -> TakeOver.VANILLA_AND_MADPARTICLE.contains(particle.getClass()));
            }
            stream.forEach(particle -> {
                particle.tick();
                if (!particle.isAlive()) {
                    removeCache.put(particle, NULL);
                }
            });
        }).join();
        //tick other mods' particles if needed
        if (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking == TakeOver.VANILLA) {
            particles.parallelStream()
                    .filter(particle -> !TakeOver.VANILLA_AND_MADPARTICLE.contains(particle.getClass()))
                    .sequential()
                    .forEach(particle -> {
                        particle.tick();
                        if (!particle.isAlive()) {
                            removeCache.put(particle, NULL);
                        }
                    });
        }
        var r = removeCache.asMap().keySet();
        particles.removeAll(r);
        InstancedRenderManager.removeAll(r);
        removeCache.invalidateAll();
    }
}
