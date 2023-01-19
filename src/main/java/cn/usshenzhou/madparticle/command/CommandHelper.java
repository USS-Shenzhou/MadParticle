package cn.usshenzhou.madparticle.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;

/**
 * @author USS_Shenzhou
 */
public class CommandHelper {

    public static <S, C> CommandContext<S> getContextHasArgument(CommandContext<S> root, String argument, Class<C> clazz) {
        CommandContext<S> now = root;
        while (true) {
            try {
                now.getArgument(argument, clazz);
                break;
            } catch (IllegalArgumentException e) {
                if (now.getChild() == null) {
                    return null;
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }

    public static <S> CommandContextBuilder<S> getContextBuilderHasArgument(CommandContextBuilder<S> rootBuilder, String argument) {
        CommandContextBuilder<S> now = rootBuilder;
        while (true) {
            if (now.getArguments().containsKey(argument)) {
                break;
            } else {
                if (now.getChild() == null) {
                    return null;
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }
}
