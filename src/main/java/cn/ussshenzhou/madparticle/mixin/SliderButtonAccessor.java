package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SliderButton.class)
public interface SliderButtonAccessor {

    @Mutable
    @Accessor
    void setOption(ProgressOption option);

    @Mutable
    @Accessor
    void setTooltip(List<FormattedCharSequence> tooltip);
}
