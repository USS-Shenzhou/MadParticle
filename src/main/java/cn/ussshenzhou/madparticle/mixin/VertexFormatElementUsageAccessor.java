package cn.ussshenzhou.madparticle.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage.ClearState;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage.SetupState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VertexFormatElement.Usage.class)
public interface VertexFormatElementUsageAccessor{
        @Invoker("<init>")
        static VertexFormatElement.Usage constructor(String enumName, int enumIndex, String name, SetupState steup, ClearState clear){
            return null;
        }
}