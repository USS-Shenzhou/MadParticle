package cn.usshenzhou.madparticle.command.inheritable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * @author USS_Shenzhou
 * @see IntegerArgumentType
 * This class is modified from IntegerArgumentType in Brigadier by Mojang under MIT license.
 */
public class InheritableIntegerArgument implements ArgumentType<Integer> {
    private final int minimum;
    private final int maximum;
    private final int fatherCommandParameterAmount;
    private final IntegerArgumentType integerArgumentType;

    public InheritableIntegerArgument(int minimum, int maximum, int fatherCommandParameterAmount) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.fatherCommandParameterAmount = fatherCommandParameterAmount;
        this.integerArgumentType = IntegerArgumentType.integer();
    }

    public static InheritableIntegerArgument inheritableInteger(int minimum, int maximum,int fatherCommandParameterAmount) {
        return new InheritableIntegerArgument(minimum, maximum, fatherCommandParameterAmount);
    }
    public static InheritableIntegerArgument inheritableInteger(int minimum, int maximum) {
        return new InheritableIntegerArgument(minimum, maximum, 0);
    }

    public static InheritableIntegerArgument inheritableInteger(int fatherCommandParameterAmount) {
        return new InheritableIntegerArgument(Integer.MIN_VALUE, Integer.MAX_VALUE, fatherCommandParameterAmount);
    }

    public static InheritableIntegerArgument inheritableInteger() {
        return new InheritableIntegerArgument(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
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
        return integerArgumentType.parse(reader);
    }

    private int inheritableParse(StringReader reader) throws CommandSyntaxException {
        InheritableStringReader inheritableStringReader = new InheritableStringReader(reader);
        int result = integerArgumentType.parse(inheritableStringReader);
        if (result == Integer.MAX_VALUE) {
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
        return 31 * minimum + maximum + 20010116 * fatherCommandParameterAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final InheritableIntegerArgument that)) {
            return false;
        }
        return maximum == that.maximum && minimum == that.minimum && fatherCommandParameterAmount == that.fatherCommandParameterAmount;
    }
}
