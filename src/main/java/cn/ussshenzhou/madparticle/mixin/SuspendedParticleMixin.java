package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.core.particles.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
@Mixin(targets = "net/minecraft/client/particle/SuspendedParticle$SporeBlossomAirProvider$1")
public class SuspendedParticleMixin {

    @Inject(method = "getParticleGroup", at = @At("RETURN"), cancellable = true)
    private void madparticleCancelSporeBlossomAirLimit(CallbackInfoReturnable<Optional<ParticleGroup>> cir) {
        if (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering != TakeOver.NONE) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
