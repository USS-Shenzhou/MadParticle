package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.advanced.TSuggestedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TTitledSuggestedEditBox extends TTitledComponent<TSuggestedEditBox> {
    public TTitledSuggestedEditBox(Component titleText, TSuggestedEditBox component) {
        super(titleText, component);
    }

    public TTitledSuggestedEditBox(Component titleText, Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        super(titleText, new TSuggestedEditBox(consumer));
    }

    public TTitledSuggestedEditBox(Component titleText, CommandDispatcher<CommandSourceStack> dispatcher) {
        super(titleText, new TSuggestedEditBox(dispatcher));
    }

    @Override
    public Vec2i getPreferredSize() {
        return new Vec2i(
                widget.getPreferredSize().x,
                title.getHeight() + widget.getPreferredSize().y
        );
    }
}
