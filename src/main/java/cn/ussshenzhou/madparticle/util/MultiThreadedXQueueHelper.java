package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;

import java.util.Queue;

public class MultiThreadedXQueueHelper {

    public static Queue<Particle> createByType(ParticleRenderType particleRenderType, int max) {
        if (instanced(particleRenderType)) {
            return new MultiThreadedObjectArrayListQueue<>(max);
        }
        return new MultiThreadedEqualObjectOpenHashSetQueue<>(max);
    }

    public static boolean instanced(ParticleRenderType renderType) {
        return renderType == ModParticleRenderTypes.INSTANCED || renderType == ModParticleRenderTypes.INSTANCED_TERRAIN || renderType == ModParticleRenderTypes.INSTANCED_ITEM;
    }
}
