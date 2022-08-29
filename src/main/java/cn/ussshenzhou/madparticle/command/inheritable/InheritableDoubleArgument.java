package cn.ussshenzhou.madparticle.command.inheritable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * @author USS_Shenzhou
 */
public class InheritableDoubleArgument implements ArgumentType<Double> {
    private final double minimum;
    private final double maximum;
    private final int fatherCommandParameterAmount;
    private final DoubleArgumentType doubleArgumentType;

    public InheritableDoubleArgument(double minimum, double maximum, int fatherCommandParameterAmount) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.fatherCommandParameterAmount = fatherCommandParameterAmount;
        this.doubleArgumentType = DoubleArgumentType.doubleArg(minimum, maximum);
    }

    public static InheritableDoubleArgument inheritableDouble(double minimum, double maximum) {
        return new InheritableDoubleArgument(minimum, maximum, 0);
    }

    public static InheritableDoubleArgument inheritableDouble(int fatherCommandParameterAmount) {
        return new InheritableDoubleArgument(-Double.MAX_VALUE, Double.MAX_VALUE, fatherCommandParameterAmount);
    }

    public static InheritableDoubleArgument inheritableDouble() {
        return new InheritableDoubleArgument(-Double.MAX_VALUE, Double.MAX_VALUE, 0);
    }

    @Override
    public Double parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String command = reader.getString();
        String[] cut = command.split(" ");
        if (cut.length > fatherCommandParameterAmount) {
            int l = 0;
            for (int i = 0; i < cut.length; i++) {
                l = l + cut[i].length() + 1;
                if (l >= start) {
                    if (i > fatherCommandParameterAmount) {
                        return inheritableParse(reader);
                    }
                    break;
                }
            }
        }
        return doubleArgumentType.parse(reader);
    }

    private double inheritableParse(StringReader reader) throws CommandSyntaxException {
        InheritableStringReader inheritableStringReader = new InheritableStringReader(reader);
        double result = doubleArgumentType.parse(inheritableStringReader);
        reader.setCursor(inheritableStringReader.getCursor());
        return result;
    }

    @Override
    public int hashCode() {
        return (int)(31 * minimum + maximum + 20010116 * fatherCommandParameterAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final InheritableDoubleArgument that)) {
            return false;
        }
        return maximum == that.maximum && minimum == that.minimum && fatherCommandParameterAmount == that.fatherCommandParameterAmount;
    }
}
