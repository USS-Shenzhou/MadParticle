package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class LayoutHelper {


    public static void BRightOfA(TWidget a, int gap, TWidget b, int width, int height) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), width, height);
    }

    public static void BRightOfA(TWidget a, int gap, TWidget b, Size size) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), size);
    }

    public static void BRightOfA(TWidget a, int gap, TWidget b) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), a.getSize());
    }

    public static void BLeftOfA(TWidget a, int gap, TWidget b, int width, int height) {
        b.setAbsBounds(a.getX() - gap - width, a.getY(), width, height);
    }

    public static void BLeftOfA(TWidget a, int gap, TWidget b, Size size) {
        b.setAbsBounds(a.getX() - gap - size.x, a.getY(), size);
    }

    public static void BLeftOfA(TWidget a, int gap, TWidget b) {
        b.setAbsBounds(a.getX() - gap - a.getSize().x, a.getY(), a.getSize());
    }

    public static void BTopOfA(TWidget a, int gap, TWidget b, int width, int height) {
        b.setAbsBounds(a.getX(), a.getY() - height - gap, width, height);
    }

    public static void BTopOfA(TWidget a, int gap, TWidget b, Size size) {
        b.setAbsBounds(a.getX(), a.getY() - size.x - gap, size);
    }

    public static void BTopOfA(TWidget a, int gap, TWidget b) {
        b.setAbsBounds(a.getX(), a.getY() - b.getSize().y - gap, a.getSize());
    }

    public static void BBottomOfA(TWidget a, int gap, TWidget b, int width, int height) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, width, height);
    }

    public static void BBottomOfA(TWidget a, int gap, TWidget b, Size size) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, size);
    }

    public static void BBottomOfA(TWidget a, int gap, TWidget b) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, a.getSize());
    }

    public static void BSameAsA(TWidget a, TWidget b) {
        b.setAbsBounds(a.getX(), a.getY(), a.getSize());
    }
}
