package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSelectList;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class CommandStringSelectList extends TTitledSelectList<CommandStringSelectList.SubCommand> {
    public CommandStringSelectList() {
        super(new TranslatableComponent("gui.mp.de.helper.command_string"));

    }

    public class SubCommand{

    }
}
