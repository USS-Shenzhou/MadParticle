package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class HelperModePanel extends TPanel {
    private final TButton copy = new TButton(new TranslatableComponent("gui.mp.de.helper.copy"));
    private final TEditBox command = new TEditBox(new TranslatableComponent("gui.mp.de.helper.command"));

    public HelperModePanel() {
        super();
        this.add(copy);
        this.add(command);
        command.setFocus(false);
    }

    @Override
    public void layout() {
        copy.setBounds(width - copy.getPreferredSize().x, (40 - copy.getPreferredSize().y) / 2);
        LayoutHelper.BLeftOfA(copy, DesignerScreen.GAP, command, width - copy.getWidth() - DesignerScreen.GAP, copy.getPreferredSize().y);
        super.layout();
    }
}
