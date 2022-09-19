package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSimpleConstrainedEditBox;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class TTitledSimpleConstrainedEditBox extends TTitledComponent<TSimpleConstrainedEditBox> {
    public TTitledSimpleConstrainedEditBox(Component titleText, TSimpleConstrainedEditBox component) {
        super(titleText, component);
    }

    public TTitledSimpleConstrainedEditBox(Component titleText, ArgumentType<?> argument) {
        this(titleText, new TSimpleConstrainedEditBox(argument));
    }

    @Override
    public Vec2i getPreferredSize() {
        return new Vec2i(
                widget.getPreferredSize().x,
                title.getHeight() + widget.getPreferredSize().y
        );
    }
}
