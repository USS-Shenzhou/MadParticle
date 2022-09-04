package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * @author USS_Shenzhou
 */
public class MWidget2TComponentHelper {

    public static <T extends AbstractWidget & TWidget> void setBounds(int x, int y, int width, int height, T that) {
        if (that.getParent() != null) {
            that.x = x + that.getParent().getX();
            that.y = y + that.getParent().getY();
        } else {
            that.x = x;
            that.y = y;
        }
        that.setWidth(width);
        that.setHeight(height);
    }
}
