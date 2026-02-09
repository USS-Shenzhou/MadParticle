package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("rawtypes")
@Mixin(ParticleGroup.class)
public class ParticleGroupMixin {

    @Shadow
    public Queue particles;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void madparticleReplaceParticleQueue(ParticleEngine engine, CallbackInfo ci) {
        this.particles = new MultiThreadedEqualObjectLinkedOpenHashSetQueue<>(16384, ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue);
    }
}
