package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.command.IndexedCommandManager;
import cn.ussshenzhou.madparticle.particle.optimize.InstancedRenderManager;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract ParticleStatus calculateParticleLevel(boolean pDecreased);

    @Inject(method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void madparticleCheckParticleGenerateDistance(ParticleOptions pOptions, boolean pForce, boolean pDecreased, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, CallbackInfoReturnable<Particle> cir) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ParticleStatus particlestatus = this.calculateParticleLevel(pDecreased);
        if (pForce) {
            if (ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance) {
                if (camera.getPosition().distanceToSqr(pX, pY, pZ) > AddParticleHelper.getMaxParticleGenerateDistanceSqr()) {
                    cir.setReturnValue(null);
                }
            } else {
                cir.setReturnValue(this.minecraft.particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed));
            }
        } else if (camera.getPosition().distanceToSqr(pX, pY, pZ) > AddParticleHelper.getNormalParticleGenerateDistanceSqr()) {
            cir.setReturnValue(null);
        } else {
            cir.setReturnValue(particlestatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed));
        }
    }

    @Inject(method = "allChanged",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;graphicsChanged()V"))
    private void madparticleReload(CallbackInfo ci) {
        InstancedRenderManager.clear();
        IndexedCommandManager.clear();
    }
}
