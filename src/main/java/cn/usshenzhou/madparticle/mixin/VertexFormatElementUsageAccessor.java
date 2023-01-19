package cn.usshenzhou.madparticle.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage.ClearState;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage.SetupState;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author zomb-676
 */
@Mixin(VertexFormatElement.Usage.class)
public interface VertexFormatElementUsageAccessor {
    @SuppressWarnings("Contract")
    @Contract(pure = true, value = "_,_,_,_,_->new")
    @Invoker("<init>")
    static VertexFormatElement.Usage constructor(String enumName, int enumIndex, String name, SetupState steup, ClearState clear) {
        return null;
    }
}