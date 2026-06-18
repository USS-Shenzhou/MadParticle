package cn.ussshenzhou.madparticle.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPipeline.Builder.class)
public class RenderPipelineBuilderMixin {

    @ModifyExpressionValue(method = "build", at = @At(value = "INVOKE", target = "Ljava/util/Optional;equals(Ljava/lang/Object;)Z"))
    private boolean madparticleSupportSeparateBlend(boolean original) {
        return true;
    }

}
