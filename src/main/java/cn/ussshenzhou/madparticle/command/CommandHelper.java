package cn.ussshenzhou.madparticle.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.logging.LogUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class CommandHelper {

    public static <S, C> @Nullable CommandContext<S> getContextHasArgument(CommandContext<S> root, String argument, Class<C> clazz) {
        CommandContext<S> now = root;
        while (true) {
            try {
                now.getArgument(argument, clazz);
                break;
            } catch (IllegalArgumentException e) {
                if (now.getChild() == null) {
                    try {
                        Field f = now.getClass().getDeclaredField("arguments");
                        f.setAccessible(true);
                        Map<String, ParsedArgument<?, ?>> map = (Map<String, ParsedArgument<?, ?>>) f.get(now);
                        String[] s = map.keySet().toArray(new String[0]);
                        LogUtils.getLogger().error("Failed to parse command correctly after {}", Arrays.toString(s));
                    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e1) {
                        LogUtils.getLogger().error("Failed to parse command correctly. Failed to get more info.");
                    }
                    return null;
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }

    public static <S, C> @Nullable CommandContext<S> nextContextHasArgument(CommandContext<S> root, String argument, Class<C> clazz) {
        CommandContext<S> now = root.getChild();
        while (true) {
            try {
                now.getArgument(argument, clazz);
                break;
            } catch (IllegalArgumentException e) {
                if (now.getChild() == null) {
                    try {
                        Field f = now.getClass().getDeclaredField("arguments");
                        f.setAccessible(true);
                        Map<String, ParsedArgument<?, ?>> map = (Map<String, ParsedArgument<?, ?>>) f.get(now);
                        String[] s = map.keySet().toArray(new String[0]);
                        LogUtils.getLogger().error("Failed to parse command correctly after {}", Arrays.toString(s));
                    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e1) {
                        LogUtils.getLogger().error("Failed to parse command correctly. Failed to get more info.");
                    }
                    return null;
                } else {
                    now = now.getChild();
                }
            } catch (NullPointerException ignored) {
                return null;
            }
        }
        return now;
    }

    public static <S> @Nullable CommandContextBuilder<S> getContextBuilderHasArgument(CommandContextBuilder<S> rootBuilder, String argument) {
        CommandContextBuilder<S> now = rootBuilder;
        while (true) {
            if (now.getArguments().containsKey(argument)) {
                break;
            } else {
                if (now.getChild() == null) {
                    LogUtils.getLogger().error("Failed to parse command correctly after {}", now.getArguments().keySet());
                    return null;
                } else {
                    now = now.getChild();
                }
            }
        }
        return now;
    }
}
