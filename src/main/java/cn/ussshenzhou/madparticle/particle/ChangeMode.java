package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.util.MathHelper;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ChangeMode {

    LINEAR((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (end - begin) * x;
    }),
    INDEX((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (float) ((end - begin) * (Math.pow(ChangeMode.BASE, x) - 1) / (ChangeMode.BASE - 1));
    }),
    SIN((begin, end, age, life) -> {
        float x = age / (float) life;
        return begin + (end - begin) * MathHelper.getSin01(x);
    }),
    INHERIT((begin, end, age, life) -> 0.0f);

    private static final int BASE = 10;
    @FunctionalInterface
    interface LerpFunction<A, B, C, D, R> {
        @SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
        public R apply(A begin, B end, C age, D lifeTime);
    }

    private final LerpFunction<Float, Float, Integer, Integer, Float> lerp;

    ChangeMode(LerpFunction<Float, Float, Integer, Integer, Float> lerpFunction) {
        this.lerp = lerpFunction;
    }

    public float lerp(float begin, float end, int age, int life) {
        return this.lerp.apply(begin, end, age, life);
    }

}
