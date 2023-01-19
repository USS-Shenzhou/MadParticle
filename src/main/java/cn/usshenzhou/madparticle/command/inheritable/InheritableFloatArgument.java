package cn.usshenzhou.madparticle.command.inheritable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * @author USS_Shenzhou
 * @see FloatArgumentType
 * This class is modified from FloatArgumentType in Brigadier by Mojang under MIT license.
 */
public class InheritableFloatArgument implements ArgumentType<Float> {
    private final float minimum;
    private final float maximum;
    private final int fatherCommandParameterAmount;
    private final FloatArgumentType floatArgumentType;

    public InheritableFloatArgument(float minimum, float maximum, int fatherCommandParameterAmount) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.fatherCommandParameterAmount = fatherCommandParameterAmount;
        this.floatArgumentType = FloatArgumentType.floatArg();
    }

    public static InheritableFloatArgument inheritableFloat(float minimum, float maximum) {
        return new InheritableFloatArgument(minimum, maximum, 0);
    }

    public static InheritableFloatArgument inheritableFloat(int fatherCommandParameterAmount) {
        return new InheritableFloatArgument(-Float.MAX_VALUE, Float.MAX_VALUE, fatherCommandParameterAmount);
    }

    public static InheritableFloatArgument inheritableFloat() {
        return new InheritableFloatArgument(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
    }

    @Override
    public Float parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String command = reader.getString();
        String[] cut = command.split(" ");
        if (cut.length > fatherCommandParameterAmount) {
            int l = 0;
            for (int i = 0; i < cut.length; i++) {
                l = l + cut[i].length() + 1;
                if (l >= start) {
                    if (i >= fatherCommandParameterAmount) {
                        return inheritableParse(reader);
                    }
                    break;
                }
            }
        }
        return floatArgumentType.parse(reader);
    }

    private float inheritableParse(StringReader reader) throws CommandSyntaxException {
        InheritableStringReader inheritableStringReader = new InheritableStringReader(reader);
        float result = floatArgumentType.parse(inheritableStringReader);
        if (result == Float.MAX_VALUE) {
            reader.setCursor(inheritableStringReader.getCursor());
            return result;
        } else if (result < minimum) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooLow().createWithContext(reader, result, minimum);
        } else if (result > maximum) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooHigh().createWithContext(reader, result, maximum);
        }
        reader.setCursor(inheritableStringReader.getCursor());
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (31 * minimum + maximum + 20010116 * fatherCommandParameterAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final InheritableFloatArgument that)) {
            return false;
        }
        return maximum == that.maximum && minimum == that.minimum && fatherCommandParameterAmount == that.fatherCommandParameterAmount;
    }
}
