package cn.ussshenzhou.madparticle.particle;


import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.gui.util.ToTranslatableString;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ChangeMode implements ToTranslatableString {

    LINEAR((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (end - begin) * x;
    }, "gui.mp.de.helper.linear"),
    INDEX((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (float) ((end - begin) * (Math.pow(ChangeMode.BASE, x) - 1) / (ChangeMode.BASE - 1));
    }, "gui.mp.de.helper.index"),
    SIN((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (end - begin) * MathHelper.getSin01(x);
    }, "gui.mp.de.helper.sin"),
    INHERIT((begin, end, age, life) -> 0.0f, "gui.mp.de.helper.inherit");

    private static final int BASE = 10;
    private final String translateKey;

    @FunctionalInterface
    interface LerpFunction<A, B, C, D, R> {
        @SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
        public R apply(A begin, B end, C age, D lifeTime);
    }

    private final LerpFunction<Float, Float, Integer, Integer, Float> lerp;

    ChangeMode(LerpFunction<Float, Float, Integer, Integer, Float> lerpFunction, String translateKey) {
        this.lerp = lerpFunction;
        this.translateKey = translateKey;
    }

    public float lerp(float begin, float end, int age, int life) {
        return this.lerp.apply(begin, end, age, life);
    }

    @Override
    public String toTranslateKey() {
        return translateKey;
    }

}
