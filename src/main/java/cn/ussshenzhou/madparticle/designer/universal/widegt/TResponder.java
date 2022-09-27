package cn.ussshenzhou.madparticle.designer.universal.widegt;

import java.util.function.Consumer;

/**
 * @author Tony Yu
 */
public interface TResponder<T> {
    void respond(T value);

    void addResponder(Consumer<T> responder);

    void clearResponders();
}
