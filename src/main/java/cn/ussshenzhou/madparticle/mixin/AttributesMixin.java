package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(Attributes.class)
public class AttributesMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 32.0, ordinal = 0))
    private static double madparticleAllowFurtherCamera(double constant) {
        return 64;
    }
}
