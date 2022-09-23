package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TCycleButton;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TTitledCycleButton<E> extends TTitledComponent<TCycleButton<E>> {
    public TTitledCycleButton(Component titleText) {
        super(titleText, new TCycleButton<>());
    }

    public TTitledCycleButton(Component titleText, Collection<TCycleButton<E>.Entry> entries) {
        super(titleText, new TCycleButton<E>(entries));
    }

    @Override
    public Vec2i getPreferredSize() {
        return new Vec2i(
                TButton.RECOMMEND_SIZE.x,
                title.getHeight() + TButton.RECOMMEND_SIZE.y
        );
    }

    public void addElement(E e) {
        getComponent().addElement(e);
    }

    public void addElement(E e, Consumer<TCycleButton<E>> consumer) {
        getComponent().addElement(e, consumer);
    }

    public void addElement(TCycleButton<E>.Entry e) {
        getComponent().addElement(e);
    }

    public void removeElement(E e) {
        getComponent().removeElement(e);
    }


}
