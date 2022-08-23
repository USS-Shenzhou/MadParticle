package cn.ussshenzhou.madparticle.util;

import java.util.Random;

/**
 * @author USS_Shenzhou
 */
public class MathHelper {
    public static double signedRandom() {
        Random r = new Random();
        return r.nextBoolean() ? -r.nextDouble() : r.nextDouble();
    }

    public static double signedRandom(Random r) {
        return r.nextBoolean() ? -r.nextDouble() : r.nextDouble();
    }
}
