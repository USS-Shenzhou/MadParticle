package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.Size;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;


/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface TWidget extends Widget, GuiEventListener {

    boolean isVisible();

    void setVisible(boolean visible);

    void setBounds(int x, int y, int width, int height);

    void setParent(TComponent parent);

    TComponent getParent();

    default void setBounds(int x, int y, Size size) {
        setBounds(x, y, size.x, size.y);
    }

    Size getPreferredSize();

    void tick();
    //int getX();

    //int getY();
}
