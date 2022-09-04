package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TComponent;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class LayoutHelper {


    public static void BRightOfA(TComponent a, int gap, TComponent b, int width, int height) {
        b.setBounds(a.getX() + a.getWidth() + gap, a.getY(), width, height);
    }

    public static void BRightOfA(TComponent a, int gap, TComponent b, Size size) {
        b.setBounds(a.getX() + a.getWidth() + gap, a.getY(), size);
    }

    public static void BRightOfA(TComponent a, int gap, TComponent b) {
        b.setBounds(a.getX() + a.getWidth() + gap, a.getY(), a.getSize());
    }

    public static void BBottomOfA(TComponent a, int gap, TComponent b, int width, int height) {
        b.setBounds(a.getX(), a.getY() + a.getHeight() + gap, width, height);
    }

    public static void BBottomOfA(TComponent a, int gap, TComponent b, Size size) {
        b.setBounds(a.getX(), a.getY() + a.getHeight() + gap, size);
    }

    public static void BBottomOfA(TComponent a, int gap, TComponent b) {
        b.setBounds(a.getX(), a.getY() + a.getHeight() + gap, a.getSize());
    }
}
