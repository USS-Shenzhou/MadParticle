package cn.ussshenzhou.madparticle.command.inheritable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.LocalCoordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;

/**
 * @author USS_Shenzhou
 */
public class InheritableVec3Argument extends Vec3Argument {
    private final int fatherCommandParameterAmount;

    public InheritableVec3Argument(boolean pCenterCorrect, int fatherCommandParameterAmount) {
        super(pCenterCorrect);
        this.fatherCommandParameterAmount = fatherCommandParameterAmount;
    }

    public static InheritableVec3Argument inheritableVec3() {
        return new InheritableVec3Argument(true, 0);
    }

    public static InheritableVec3Argument inheritableVec3(int fatherCommandParameterAmount) {
        return new InheritableVec3Argument(true, fatherCommandParameterAmount);
    }

    @Override
    public Coordinates parse(StringReader reader) throws CommandSyntaxException {
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
        return normalParse(reader);
    }
    private Coordinates normalParse(StringReader reader) throws CommandSyntaxException {
        return (Coordinates) (reader.canRead() && reader.peek() == '^' ? LocalCoordinates.parse(reader) : WorldCoordinates.parseDouble(reader, true));
    }

    private Coordinates inheritableParse(StringReader reader) throws CommandSyntaxException {
        InheritableStringReader iReader = new InheritableStringReader(reader);
        Coordinates result = (Coordinates) (iReader.canRead() && iReader.peek() == '^' ? LocalCoordinates.parse(iReader) : WorldCoordinates.parseDouble(iReader, true));
        reader.setCursor(iReader.getCursor());
        return result;
    }
}
