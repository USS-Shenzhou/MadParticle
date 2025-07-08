package cn.ussshenzhou.madparticle.util;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import net.minecraft.util.RandomSource;
import org.joml.Vector2i;

import java.util.Random;

import static jdk.incubator.vector.IntVector.*;
import static jdk.incubator.vector.VectorOperators.*;

/**
 * @author USS_Shenzhou
 */
public class MathHelper {
    private static final double[] SIN01 = {0, 0.000148046502605914, 0.000592215314954070, 0.00133259484264708, 0.00236933407524353, 0.00370264508073576, 0.00533280654674539, 0.00726016841886612, 0.00948515770320879, 0.0120082855186116, 0.0148301555047160, 0.0179514737158149, 0.0213730601578324, 0.0250958621579424, 0.0291209697943535, 0.0334496336591418, 0.0380832852815755, 0.0430235606054795, 0.0482723269948982, 0.0538317143415242, 0.0597041509702024, 0.0658924051920205, 0.0723996335469677, 0.0792294370218563, 0.0863859268402791, 0.0938738018218823, 0.101698439828584, 0.109866006498055, 0.118383585369840, 0.127259334722979, 0.136502678090967, 0.146124537684986, 0.156137623117013, 0.166556792295047, 0.177399507826531, 0.188686421772719, 0.200442135882674, 0.212696206427475, 0.225484497543808, 0.238851043759635, 0.252850678352293, 0.267552853332525, 0.283047389835880, 0.299453511720029, 0.316934808940907, 0.335725759051570, 0.356183157019473, 0.378899259661155, 0.405003752527172, 0.437307287109167, 0.500000000000000, 0.562692712890833, 0.594996247472828, 0.621100740338845, 0.643816842980528, 0.664274240948431, 0.683065191059093, 0.700546488279971, 0.716952610164120, 0.732447146667475, 0.747149321647707, 0.761148956240365, 0.774515502456192, 0.787303793572525, 0.799557864117326, 0.811313578227281, 0.822600492173469, 0.833443207704953, 0.843862376882987, 0.853875462315014, 0.863497321909034, 0.872740665277021, 0.881616414630160, 0.890133993501945, 0.898301560171416, 0.906126198178118, 0.913614073159721, 0.920770562978144, 0.927600366453032, 0.934107594807980, 0.940295849029798, 0.946168285658476, 0.951727673005102, 0.956976439394521, 0.961916714718425, 0.966550366340858, 0.970879030205647, 0.974904137842058, 0.978626939842168, 0.982048526284185, 0.985169844495284, 0.987991714481388, 0.990514842296791, 0.992739831581134, 0.994667193453255, 0.996297354919264, 0.997630665924757, 0.998667405157353, 0.999407784685046, 0.999851953497394, 1};

    public static double signedRandom(RandomSource r) {
        return r.nextDouble() * 2 - 1;
    }

    public static double signedRandom(Random r) {
        return r.nextDouble() * 2 - 1;
    }

    public static float getSin01(float x) {
        int floor = (int) Math.floor(x * 100);
        return (float) (SIN01[floor] + (SIN01[floor == 100 ? floor : floor + 1] - SIN01[floor]) * (x * 100 - floor) / 100);
    }

    public static float getFromT(float t, float[] array) {
        int max = array.length - 1;
        int floor = (int) Math.floor(t * max);
        return array[floor] + (array[floor == max ? floor : floor + 1] - array[floor]) * (t * max - floor) / max;
    }

    public static float getFromT(float t, int[] array) {
        int max = array.length - 1;
        int floor = (int) Math.floor(t * max);
        return array[floor] + (array[floor == max ? floor : floor + 1] - array[floor]) * (t * max - floor) / max;
    }

    public static Vector2i copy(Vector2i old) {
        return new Vector2i(old.x, old.y);
    }

    private static final IntVector SIGN_MASK_128 = IntVector.broadcast(SPECIES_128, 0x80000000);
    private static final IntVector NOT_SIGN_MASK_128 = IntVector.broadcast(SPECIES_128, 0x7fffffff);
    private static final IntVector ROUND_BIAS_128 = IntVector.broadcast(SPECIES_128, 0x00001000);
    private static final IntVector BIAS_DIFF_128 = IntVector.broadcast(SPECIES_128, 0x0001c000);
    private static final IntVector F16_MAX_BITS_128 = IntVector.broadcast(SPECIES_128, 0x47c00000);
    private static final IntVector SMALL_BITS_128 = IntVector.broadcast(SPECIES_128, Float.floatToRawIntBits(1.0e-4f));
    private static final IntVector EXP_MASK_128 = IntVector.broadcast(SPECIES_128, 0x7f800000);
    private static final IntVector INF16_128 = IntVector.broadcast(SPECIES_128, 0x00007c00);
    private static final IntVector QNAN16_128 = IntVector.broadcast(SPECIES_128, 0x00007e00);
    private static final IntVector ZERO_128 = IntVector.zero(SPECIES_128);

    /**
     * There are about 0.22% chance that the results' error comparing to standard {@link Float#floatToFloat16(float)} are bigger than 0.0001, but always smaller than 0.5 within ±1600.
     */
    public static ShortVector toHalfFloat4(FloatVector floatVector) {
        IntVector bits = floatVector.reinterpretAsInts();
        IntVector sign16 = bits.and(SIGN_MASK_128).lanewise(LSHR, 16);
        IntVector absBits = bits.and(NOT_SIGN_MASK_128);

        VectorMask<Integer> smallMask = absBits.compare(LT, SMALL_BITS_128);
        VectorMask<Integer> specialMask = absBits.compare(GE, EXP_MASK_128);

        IntVector base = bits.add(ROUND_BIAS_128).lanewise(LSHR, 13).sub(BIAS_DIFF_128);

        VectorMask<Integer> overflowMask = absBits.compare(GT, F16_MAX_BITS_128);
        IntVector merged = base.blend(ZERO_128, smallMask).blend(INF16_128, overflowMask);
        VectorMask<Integer> nanMask = specialMask.and(absBits.compare(NE, EXP_MASK_128));
        merged = merged.blend(QNAN16_128, nanMask);

        IntVector halfBits = merged.or(sign16);

        return (ShortVector) halfBits.convertShape(I2S, ShortVector.SPECIES_64, 0);
    }

    private static final IntVector SIGN_MASK_256 = IntVector.broadcast(SPECIES_256, 0x80000000);
    private static final IntVector NOT_SIGN_MASK_256 = IntVector.broadcast(SPECIES_256, 0x7fffffff);
    private static final IntVector ROUND_BIAS_256 = IntVector.broadcast(SPECIES_256, 0x00001000);
    private static final IntVector BIAS_DIFF_256 = IntVector.broadcast(SPECIES_256, 0x0001c000);
    private static final IntVector F16_MAX_BITS_256 = IntVector.broadcast(SPECIES_256, 0x47c00000);
    private static final IntVector SMALL_BITS_256 = IntVector.broadcast(SPECIES_256, Float.floatToRawIntBits(1.0e-4f));
    private static final IntVector EXP_MASK_256 = IntVector.broadcast(SPECIES_256, 0x7f800000);
    private static final IntVector INF16_256 = IntVector.broadcast(SPECIES_256, 0x00007c00);
    private static final IntVector QNAN16_256 = IntVector.broadcast(SPECIES_256, 0x00007e00);
    private static final IntVector ZERO_256 = IntVector.zero(SPECIES_256);

    /**
     * There are about 0.22% chance that the results' error comparing to standard {@link Float#floatToFloat16(float)} are bigger than 0.0001, but always smaller than 0.5 within ±1600.
     */
    public static ShortVector toHalfFloat8(FloatVector floatVector) {
        IntVector bits = floatVector.viewAsIntegralLanes();
        IntVector sign16 = bits.and(SIGN_MASK_256).lanewise(LSHR, 16);
        IntVector absBits = bits.and(NOT_SIGN_MASK_256);

        VectorMask<Integer> smallMask = absBits.compare(LT, SMALL_BITS_256);
        VectorMask<Integer> specialMask = absBits.compare(GE, EXP_MASK_256);

        IntVector base = bits.add(ROUND_BIAS_256).lanewise(LSHR, 13).sub(BIAS_DIFF_256);

        VectorMask<Integer> overflowMask = absBits.compare(GT, F16_MAX_BITS_256);
        IntVector merged = base.blend(ZERO_256, smallMask).blend(INF16_256, overflowMask);
        VectorMask<Integer> nanMask = specialMask.and(absBits.compare(NE, EXP_MASK_256));
        merged = merged.blend(QNAN16_256, nanMask);

        IntVector halfBits = merged.or(sign16);

        return (ShortVector) halfBits.convertShape(I2S, ShortVector.SPECIES_128, 0);
    }
}
