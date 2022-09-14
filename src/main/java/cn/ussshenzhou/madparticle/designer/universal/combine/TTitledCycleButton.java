package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TCycleButton;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class TTitledCycleButton<E> extends TTitledComponent<TCycleButton<E>> {
    public TTitledCycleButton(Component titleText) {
        super(titleText, new TCycleButton<>());
    }

    public TTitledCycleButton(Component titleText, Collection<TCycleButton<E>.Entry> entries) {
        super(titleText, new TCycleButton<E>(entries));
    }
}
