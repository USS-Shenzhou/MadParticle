package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import javax.annotation.Nullable;


/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface TWidget extends Widget, GuiEventListener {

    boolean isVisible();

    void setVisible(boolean visible);

    void setBounds(int x, int y, int width, int height);

    void setAbsBounds(int x, int y, int width, int height);

    default void setBounds(int x, int y, Vec2i size) {
        setBounds(x, y, size.x, size.y);
    }

    default void setAbsBounds(int x, int y, Vec2i size) {
        setAbsBounds(x, y, size.x, size.y);
    }

    void setParent(TComponent parent);

    @Nullable
    TComponent getParent();

    int getX();

    int getY();

    Vec2i getPreferredSize();

    Vec2i getSize();

    void tick();

    default void renderTop(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
    }

    default boolean isInRange(double pMouseX, double pMouseY) {
        return isInRange(pMouseX, pMouseY, 0, 0);
    }

    default boolean isInRange(double pMouseX, double pMouseY, double xPadding, double yPadding) {
        return pMouseX >= getX() - xPadding && pMouseX <= getX() + getSize().x + xPadding && pMouseY >= getY() - yPadding && pMouseY <= getY() + getSize().y + yPadding;
    }

    default void onClose() {
    }

    @SuppressWarnings("unchecked")
    default @Nullable <T extends TWidget> T getParentInstanceOf(Class<T> c) {
        TWidget son = this;
        while (son.getParent() != null) {
            TWidget parent = son.getParent();
            if (c.isInstance(parent)) {
                return (T)parent;
            } else {
                son = parent;
            }
        }
        return null;
    }
}
