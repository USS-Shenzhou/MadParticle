package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Tony Yu
 */
@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor<E extends AbstractSelectionList.Entry<E>> {
    @Accessor
    void setHovered(E hovered);

    @Accessor
    boolean isRenderHeader();

    @Accessor
    boolean isRenderBackground();

    @Accessor
    boolean isRenderTopAndBottom();
}
