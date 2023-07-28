package cn.ussshenzhou.madparticle.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.annotation.Nullable;

import static cn.ussshenzhou.madparticle.util.MetaKeys.Util.*;

/**
 * @author USS_Shenzhou
 */

public enum MetaKeys {
    TADA("tada", BOOLEAN),
    DX("dx", EXPRESSION_ARGUMENT),
    DY("dy", EXPRESSION_ARGUMENT),
    DZ("dz", EXPRESSION_ARGUMENT),
    LIFE_ERROR("life", IntegerArgumentType.integer()),
    DISAPPEAR_ON_COLLISION("disappearOnCollision", IntegerArgumentType.integer(1)),
    TENET("tenet", BOOLEAN);

    private final String key;
    public final ArgumentType<?> inputArgument;

    MetaKeys(String key, ArgumentType<?> inputArgument) {
        this.key = key;
        this.inputArgument = inputArgument;
    }

    public String get() {
        return key;
    }

    public static @Nullable MetaKeys fromString(String key) {
        return switch (key) {
            case "tada" -> TADA;
            case "dx" -> DX;
            case "dy" -> DY;
            case "dz" -> DZ;
            case "life" -> LIFE_ERROR;
            case "disappearOnCollision" -> DISAPPEAR_ON_COLLISION;
            case "tenet" -> TENET;
            default -> null;
        };
    }

    public static class Util {
        public static final ArgumentType<String> EXPRESSION_ARGUMENT = reader -> {
            String text = reader.getString();
            try {
                Expression expression = new ExpressionBuilder(text)
                        .variable("t")
                        .build();
                if (expression.validate(false).isValid()) {
                    return text;
                } else {
                    throw new Exception();
                }
            } catch (Exception ignored) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create(null);
            }
        };

        public static final IntegerArgumentType BOOLEAN = IntegerArgumentType.integer(0, 1);
    }

}
