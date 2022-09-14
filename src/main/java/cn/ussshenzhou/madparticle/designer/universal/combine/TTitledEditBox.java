package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import net.minecraft.network.chat.Component;

public class TTitledEditBox extends TTitledComponent<TEditBox> {
    public TTitledEditBox(Component titleText, TEditBox component) {
        super(titleText, component);
    }

    public TTitledEditBox(Component titleText, Component editBoxText) {
        this(titleText, new TEditBox(editBoxText));
    }

    public TTitledEditBox(Component titleText) {
        super(titleText, new TEditBox());
    }
}
