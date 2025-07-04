package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.api.AddParticleHelperS;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ParticleStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    private void madparticleCheckParticleGenerateDistance(ParticleOptions options, boolean force, boolean decreased, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ParticleStatus particlestatus = this.calculateParticleLevel(decreased);
        if (force) {
            if (ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance) {
                if (camera.getPosition().distanceToSqr(x, y, z) > AddParticleHelperS.getMaxParticleGenerateDistanceSqr()) {
                    cir.setReturnValue(null);
                }
            } else {
                cir.setReturnValue(this.minecraft.particleEngine.createParticle(options, x, y, z, xSpeed, ySpeed, zSpeed));
            }
        } else if (camera.getPosition().distanceToSqr(x, y, z) > AddParticleHelperS.getNormalParticleGenerateDistanceSqr()) {
            cir.setReturnValue(null);
        } else {
            cir.setReturnValue(particlestatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(options, x, y, z, xSpeed, ySpeed, zSpeed));
        }
    }
}
