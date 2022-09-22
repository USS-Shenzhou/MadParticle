package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;
import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import net.minecraft.client.gui.components.EditBox;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class LayoutHelper {


    public static void BRightOfA(TWidget b, int gap, TWidget a, int width, int height) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), width, height);
    }

    public static void BRightOfA(TWidget b, int gap, TWidget a, Vec2i size) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), size);
    }

    public static void BRightOfA(TWidget b, int gap, TWidget a) {
        b.setAbsBounds(a.getX() + a.getSize().x + gap, a.getY(), a.getSize());
    }

    public static void BLeftOfA(TWidget b, int gap, TWidget a, int width, int height) {
        b.setAbsBounds(a.getX() - gap - width, a.getY(), width, height);
    }

    public static void BLeftOfA(TWidget b, int gap, TWidget a, Vec2i size) {
        b.setAbsBounds(a.getX() - gap - size.x, a.getY(), size);
    }

    public static void BLeftOfA(TWidget b, int gap, TWidget a) {
        b.setAbsBounds(a.getX() - gap - a.getSize().x, a.getY(), a.getSize());
    }

    public static void BTopOfA(TWidget b, int gap, TWidget a, int width, int height) {
        b.setAbsBounds(a.getX(), a.getY() - height - gap, width, height);
    }

    public static void BTopOfA(TWidget b, int gap, TWidget a, Vec2i size) {
        b.setAbsBounds(a.getX(), a.getY() - size.x - gap, size);
    }

    public static void BTopOfA(TWidget b, int gap, TWidget a) {
        b.setAbsBounds(a.getX(), a.getY() - b.getSize().y - gap, a.getSize());
    }

    public static void BBottomOfA(TWidget b, int gap, TWidget a, int width, int height) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, width, height);
    }

    public static void BBottomOfA(TWidget b, int gap, TWidget a, Vec2i size) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, size);
    }

    public static void BBottomOfA(TWidget b, int gap, TWidget a) {
        b.setAbsBounds(a.getX(), a.getY() + a.getSize().y + gap, a.getSize());
    }

    public static void BSameAsA(TWidget b, TWidget a) {
        b.setAbsBounds(a.getX(), a.getY(), a.getSize());
    }

    public static int getEditBoxCursorX(EditBox editBox) {
        return ((EditBoxAccessor) editBox).getDisplayPos();
    }
}
