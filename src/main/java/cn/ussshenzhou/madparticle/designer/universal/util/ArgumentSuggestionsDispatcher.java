package cn.ussshenzhou.madparticle.designer.universal.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author USS_Shenzhou
 */
public class ArgumentSuggestionsDispatcher<T> extends CommandDispatcher<CommandSourceStack> {

    public ArgumentCommandNode<CommandSourceStack,T> register(final RequiredArgumentBuilder<CommandSourceStack, T> command) {
        final ArgumentCommandNode<CommandSourceStack, T> build = command.build();
        getRoot().addChild(build);
        return build;
    }

}
