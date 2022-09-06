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

    void setAbsBounds(int x, int y, int width, int height);

    default void setBounds(int x, int y, Size size) {
        setBounds(x, y, size.x, size.y);
    }

    default void setAbsBounds(int x,int y,Size size){
        setAbsBounds(x, y, size.x, size.y);
    }
    void setParent(TComponent parent);

    TComponent getParent();

    int getX();

    int getY();

    Size getPreferredSize();

    Size getSize();

    void tick();
    //int getX();

    //int getY();
}
