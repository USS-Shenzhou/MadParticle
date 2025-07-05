package cn.ussshenzhou.madparticle.mixinproxy;

import cn.ussshenzhou.madparticle.particle.optimize.ParallelTickManager;
import com.google.common.collect.Lists;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TrackingEmitter;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class ParticleEngineMixinProxy {

    public static void tick(ParticleEngine thiz) {
        if (!thiz.trackingEmitters.isEmpty()) {
            List<TrackingEmitter> list = Lists.newArrayList();

            for (TrackingEmitter trackingemitter : thiz.trackingEmitters) {
                trackingemitter.tick();
                if (!trackingemitter.isAlive()) {
                    list.add(trackingemitter);
                }
            }

            thiz.trackingEmitters.removeAll(list);
        }
        ParallelTickManager.tick(thiz);
    }
}
