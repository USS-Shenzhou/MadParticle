package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.util.MultiThreadedEqualObjectOpenHashSetQueue;
import cn.ussshenzhou.madparticle.util.MultiThreadedXQueueHelper;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.particle.*;
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
@Mixin(QuadParticleGroup.class)
public abstract class QuadParticleGroupMixin extends ParticleGroup {

    public QuadParticleGroupMixin(ParticleEngine engine) {
        super(engine);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void madparticleReplaceParticleQueue(ParticleEngine engine, ParticleRenderType particleType, CallbackInfo ci) {
        var max = ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue;
        this.particles = MultiThreadedXQueueHelper.createByType(particleType, max);
    }
}
