package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TComponent;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class LayoutHelper {


    public static void BRightOfA(TComponent a, int gap, TWidget b, int width, int height) {
        b.setBounds(a.getRelativeX() + a.getWidth() + gap, a.getRelativeY(), width, height);
    }

    public static void BRightOfA(TComponent a, int gap, TWidget b, Size size) {
        b.setBounds(a.getRelativeX() + a.getWidth() + gap, a.getRelativeY(), size);
    }

    public static void BRightOfA(TComponent a, int gap, TWidget b) {
        b.setBounds(a.getRelativeX() + a.getWidth() + gap, a.getRelativeY(), a.getSize());
    }

    public static void BBottomOfA(TComponent a, int gap, TWidget b, int width, int height) {
        b.setBounds(a.getRelativeX(), a.getRelativeY() + a.getHeight() + gap, width, height);
    }

    public static void BBottomOfA(TComponent a, int gap, TWidget b, Size size) {
        b.setBounds(a.getRelativeX(), a.getRelativeY() + a.getHeight() + gap, size);
    }

    public static void BBottomOfA(TComponent a, int gap, TWidget b) {
        b.setBounds(a.getRelativeX(), a.getRelativeY() + a.getHeight() + gap, a.getSize());
    }
}
