package cn.usshenzhou.madparticle.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author USS_Shenzhou
 */
@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor
    boolean isFastFormat();

    @Accessor
    boolean isFullFormat();

    @Accessor
    void setNextElementByte(int nextElementByte);

    @Accessor
    int getNextElementByte();
}
