package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @ModifyConstant(method = "net/minecraft/client/particle/ParticleEngine.lambda$tick$11(Lnet/minecraft/client/particle/ParticleRenderType;)Ljava/util/Queue;", constant = @Constant(intValue = 16384), require = 0)
    private static int madparticleChangeMaxAmount(int constant) {
        return madparticleMaxAmount();
    }

    @ModifyConstant(method = "net/minecraft/client/particle/ParticleEngine.lambda$tick$11(Lnet/minecraft/client/particle/ParticleRenderType;)Ljava/util/Queue;", constant = @Constant(intValue = 16384), remap = false, require = 0)
    private static int madparticleChangeMaxAmountOptifineCompatibility(int constant) {
        return madparticleMaxAmount();
    }

    private static int madparticleMaxAmount() {
        return ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue;
    }

    @Shadow
    @Final
    private Queue<Particle> particlesToAdd;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void madparticleCancelAddWhenFull(CallbackInfo ci) {
        if (this.particlesToAdd.size() >= madparticleMaxAmount()) {
            ci.cancel();
        }
    }
}
