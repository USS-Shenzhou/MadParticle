package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @ModifyConstant(method = "runTick", constant = @Constant(intValue = 10))
    private int madparticleCancelTickChase(int constant) {
        if (ConfigHelper.getConfigRead(MadParticleConfig.class).priorityFPS) {
            return 1;
        }
        return 10;
    }

}
