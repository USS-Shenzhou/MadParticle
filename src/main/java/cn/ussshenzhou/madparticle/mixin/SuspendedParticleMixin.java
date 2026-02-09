package cn.ussshenzhou.madparticle.mixin;

import org.spongepowered.asm.mixin.Mixin;

/**
 * @author USS_Shenzhou
 */
@Mixin(targets = "net/minecraft/client/particle/SuspendedParticle$SporeBlossomAirProvider$1")
public class SuspendedParticleMixin {

    //@Inject(method = "getParticleGroup", at = @At("RETURN"), cancellable = true)
    //private void madparticleCancelSporeBlossomAirLimit(CallbackInfoReturnable<Optional<ParticleGroup>> cir) {
    //    if (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering != TakeOver.NONE) {
    //        cir.setReturnValue(Optional.empty());
    //    }
    //}
}
