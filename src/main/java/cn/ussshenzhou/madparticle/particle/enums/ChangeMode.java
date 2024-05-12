package cn.ussshenzhou.madparticle.particle.enums;


import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ChangeMode implements ITranslatable {

    LINEAR("gui.mp.de.helper.linear"),
    INDEX("gui.mp.de.helper.index"),
    SIN("gui.mp.de.helper.sin"),
    INHERIT("gui.mp.de.helper.inherit");

    private static final int BASE = 10;
    private final String translateKey;

    ChangeMode(String translateKey) {
        this.translateKey = translateKey;
    }

    /**
     * Stop use Function: too many memory consumed.
     */
    public float lerp(float begin, float end, int age, int life) {
        float x = age / (float) life;
        return switch (this) {
            case LINEAR -> begin + (end - begin) * x;
            case INDEX -> begin + (float) ((end - begin) * (Math.pow(ChangeMode.BASE, x) - 1) / (ChangeMode.BASE - 1));
            case SIN -> begin + (end - begin) * MathHelper.getSin01(x);
            case INHERIT -> 0;
        };
    }

    @Override
    public String translateKey() {
        return translateKey;
    }

}
