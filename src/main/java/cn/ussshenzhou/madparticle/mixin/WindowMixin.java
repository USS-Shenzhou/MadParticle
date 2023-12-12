package cn.ussshenzhou.madparticle.mixin;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author USS_Shenzhou
 */
@Mixin(Window.class)
public class WindowMixin {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/loading/progress/EarlyProgressVisualization;handOffWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J")
            ),
            remap = false,
            require = 0, expect = 0
    )
    private int madparticleUseOpenGl33(int constant) {
        return 3;
    }
}
