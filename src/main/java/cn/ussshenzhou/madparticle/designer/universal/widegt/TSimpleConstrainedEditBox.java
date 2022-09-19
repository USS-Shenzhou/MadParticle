package cn.ussshenzhou.madparticle.designer.universal.widegt;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * @author USS_Shenzhou
 */
public class TSimpleConstrainedEditBox extends TEditBox {
    private ArgumentType<?> argument;

    public TSimpleConstrainedEditBox(ArgumentType<?> argument) {
        super();
        this.argument = argument;
        this.setResponder(this::check);
    }

    public void check(String value) {
        try {
            StringReader stringReader = new StringReader(value);
            argument.parse(stringReader);
            if (stringReader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(stringReader);
            }
        } catch (CommandSyntaxException e) {
            this.setTextColor(0xfc5454);
            return;
        } catch (StringIndexOutOfBoundsException ignored) {
        }
        this.setTextColor(14737632);
    }

    public ArgumentType<?> getArgument() {
        return argument;
    }

    public void setArgument(ArgumentType<?> argument) {
        this.argument = argument;
    }
}
