package cn.ussshenzhou.madparticle.designer.universal.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;

/**
 * @author USS_Shenzhou
 */
public class ArgumentSuggestionsDispatcher<S, T> extends CommandDispatcher<S> {

    public ArgumentCommandNode<S, T> register(final RequiredArgumentBuilder<S, T> command) {
        final ArgumentCommandNode<S, T> build = command.build();
        getRoot().addChild(build);
        return build;
    }

}
