package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @ModifyConstant(method = "net/minecraft/client/particle/ParticleEngine.lambda$tick$9(Lnet/minecraft/client/particle/ParticleRenderType;)Ljava/util/Queue;", constant = @Constant(intValue = 16384))
    private static int madparticleChangeMaxAmount(int constant) {
        return ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue;
    }
}
