package cn.ussshenzhou.madparticle.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class CommandHelper {

    public static <S> CommandContext<S> getContextHasArgument(CommandContext<S> root, String argument) throws CommandSyntaxException {
        CommandContext<S> now = root;
        while (true) {
            try {
                now.getArgument(argument, ParticleOptions.class);
                break;
            } catch (IllegalArgumentException ignored) {
                if (now.getChild() == null) {
                    try {
                        Field f = now.getClass().getDeclaredField("arguments");
                        f.setAccessible(true);
                        Map<String, ParsedArgument<?, ?>> map = (Map<String, ParsedArgument<?, ?>>) f.get(now);
                        String[] s = map.keySet().toArray(new String[0]);
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(
                                "Failed to parse command correctly after " + Arrays.toString(s));
                    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException ignored1) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(
                                "Failed to parse command correctly. Failed to get more info.");
                    }
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }

    public static <S> CommandContextBuilder<S> getContextBuilderHasArgument(CommandContextBuilder<S> rootBuilder, String argument) throws CommandSyntaxException {
        CommandContextBuilder<S> now = rootBuilder;
        while (true) {
            if (now.getArguments().containsKey(argument)) {
                break;
            } else {
                if (now.getChild() == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Failed to parse command correctly after " + now.getArguments().keySet());
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }
}
