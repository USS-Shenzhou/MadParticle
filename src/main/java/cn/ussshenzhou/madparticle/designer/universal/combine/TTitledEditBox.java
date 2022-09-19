package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
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

    @Override
    public Vec2i getPreferredSize() {
        return new Vec2i(
                widget.getPreferredSize().x,
                title.getHeight() + widget.getPreferredSize().y
        );
    }
}