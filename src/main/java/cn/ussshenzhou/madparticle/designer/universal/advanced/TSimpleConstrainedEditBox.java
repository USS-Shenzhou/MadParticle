package cn.ussshenzhou.madparticle.designer.universal.advanced;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * @author USS_Shenzhou
 */
public class TSimpleConstrainedEditBox extends TConstrainedEditBox {
    private ArgumentType<?> argument;

    public TSimpleConstrainedEditBox(ArgumentType<?> argument) {
        super();
        this.argument = argument;
        this.setResponder(this::check);
    }

    @Override
    public void checkAndThrow(String value) throws CommandSyntaxException {
        StringReader stringReader = new StringReader(value);
        argument.parse(stringReader);
        if (stringReader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(stringReader);
        }
    }

    public ArgumentType<?> getArgument() {
        return argument;
    }

    public void setArgument(ArgumentType<?> argument) {
        this.argument = argument;
    }
}
