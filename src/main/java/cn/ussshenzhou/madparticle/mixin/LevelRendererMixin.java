package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyConstant(method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", constant = @Constant(doubleValue = 1024))
    private double madparticleIncreaseParticleVisibleDistance(double original) {
        return (Minecraft.getInstance().options.renderDistance * 4 * Minecraft.getInstance().options.renderDistance * 4);
    }
}
