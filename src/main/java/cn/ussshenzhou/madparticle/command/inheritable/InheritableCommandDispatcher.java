package cn.ussshenzhou.madparticle.command.inheritable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.*;

/**
 * @author USS_Shenzhou
 * @see com.mojang.brigadier.CommandDispatcher
 * This class is modified from CommandDispatcher in Brigadier by Mojang under MIT license.
 */
public class InheritableCommandDispatcher<S> extends CommandDispatcher<S> {
    public InheritableCommandDispatcher(RootCommandNode<S> root) {
        super(root);
    }

    @Deprecated
    @Override
    public ParseResults<S> parse(String command, S source) {
        return this.parse(new StringReader(command), source);
    }

    @Override
    public ParseResults<S> parse(StringReader command, S source) {
        final CommandContextBuilder<S> context = new CommandContextBuilder<>(this, source, getRoot(), command.getCursor());
        return this.parseNodes(getRoot(), command, context);
    }

    private ParseResults<S> parseNodes(final CommandNode<S> node, final StringReader originalReader, final CommandContextBuilder<S> contextSoFar) {
        final S source = contextSoFar.getSource();
        Map<CommandNode<S>, CommandSyntaxException> errors = null;
        List<ParseResults<S>> potentials = null;
        final int cursor = originalReader.getCursor();

        for (final CommandNode<S> child : node.getRelevantNodes(originalReader)) {
            if (!child.canUse(source)) {
                continue;
            }
            final CommandContextBuilder<S> context = contextSoFar.copy();
            //main change here
            final StringReader reader = originalReader instanceof InheritableStringReader ? new InheritableStringReader(originalReader) : new StringReader(originalReader);
            try {
                try {
                    child.parse(reader, context);
                } catch (final RuntimeException ex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, ex.getMessage());
                }
                if (reader.canRead()) {
                    if (reader.peek() != ARGUMENT_SEPARATOR_CHAR) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
                    }
                }
            } catch (final CommandSyntaxException ex) {
                if (errors == null) {
                    errors = new LinkedHashMap<>();
                }
                errors.put(child, ex);
                reader.setCursor(cursor);
                continue;
            }

            context.withCommand(child.getCommand());
            if (reader.canRead(child.getRedirect() == null ? 2 : 1)) {
                reader.skip();
                if (child.getRedirect() != null) {
                    final CommandContextBuilder<S> childContext = new CommandContextBuilder<>(this, source, child.getRedirect(), reader.getCursor());
                    final ParseResults<S> parse = parseNodes(child.getRedirect(), reader, childContext);
                    context.withChild(parse.getContext());
                    return new ParseResults<>(context, parse.getReader(), parse.getExceptions());
                } else {
                    final ParseResults<S> parse = parseNodes(child, reader, context);
                    if (potentials == null) {
                        potentials = new ArrayList<>(1);
                    }
                    potentials.add(parse);
                }
            } else {
                if (potentials == null) {
                    potentials = new ArrayList<>(1);
                }
                potentials.add(new ParseResults<>(context, reader, Collections.emptyMap()));
            }
        }

        if (potentials != null) {
            if (potentials.size() > 1) {
                potentials.sort((a, b) -> {
                    if (!a.getReader().canRead() && b.getReader().canRead()) {
                        return -1;
                    }
                    if (a.getReader().canRead() && !b.getReader().canRead()) {
                        return 1;
                    }
                    if (a.getExceptions().isEmpty() && !b.getExceptions().isEmpty()) {
                        return -1;
                    }
                    if (!a.getExceptions().isEmpty() && b.getExceptions().isEmpty()) {
                        return 1;
                    }
                    return 0;
                });
            }
            return potentials.get(0);
        }

        return new ParseResults<>(contextSoFar, originalReader, errors == null ? Collections.emptyMap() : errors);
    }
}
