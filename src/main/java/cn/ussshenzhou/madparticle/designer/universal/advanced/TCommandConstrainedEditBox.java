package cn.ussshenzhou.madparticle.designer.universal.advanced;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class TCommandConstrainedEditBox extends TConstrainedEditBox {

    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

    public TCommandConstrainedEditBox(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        super();
        consumer.accept(dispatcher);
    }

    @Override
    public void checkAndThrow(String value) throws CommandSyntaxException {
        if (value.startsWith("/")) {
            value = value.replaceFirst("/", "");
        }
        CommandSourceStack sourceStack = Minecraft.getInstance().player.createCommandSourceStack();
        ParseResults<CommandSourceStack> parseResults = dispatcher.parse(value, sourceStack);
        Map<?, CommandSyntaxException> map = parseResults.getExceptions();
        if ((!map.isEmpty()) || parseResults.getContext().build(value).getNodes().isEmpty()) {
            Optional<CommandSyntaxException> optional = map.values().stream().findAny();
            if (optional.isPresent()) {
                throw optional.get();
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
            }
        }
    }
}
