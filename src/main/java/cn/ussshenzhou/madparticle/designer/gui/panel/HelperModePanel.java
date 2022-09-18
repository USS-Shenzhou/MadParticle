package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.widegt.CommandStringSelectList;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class HelperModePanel extends TPanel {
    private final TButton copy = new TButton(new TranslatableComponent("gui.mp.de.helper.copy"));
    private final TEditBox command = new TEditBox(new TranslatableComponent("gui.mp.de.helper.command"));
    private final CommandStringSelectList commandStringSelectList = new CommandStringSelectList();

    ParametersScrollPanel parametersScrollPanel = new ParametersScrollPanel();

    public HelperModePanel() {
        super();
        command.setFocus(false);
        command.setMaxLength(32500);
        this.add(copy);
        this.add(command);
        this.add(commandStringSelectList);
        this.add(parametersScrollPanel);

        copy.setOnPress(pButton -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(command.getValue());
            //TODO: copied!
        });
    }

    @Override
    public void layout() {
        copy.setBounds(width - TButton.RECOMMEND_SIZE.x, (40 - TButton.RECOMMEND_SIZE.y) / 2);
        LayoutHelper.BLeftOfA(command, DesignerScreen.GAP, copy, width - copy.getWidth() - DesignerScreen.GAP, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BBottomOfA(commandStringSelectList, DesignerScreen.GAP, command,
                TButton.RECOMMEND_SIZE.x + commandStringSelectList.getComponent().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - command.getY() - command.getHeight() - DesignerScreen.GAP * 2 - TButton.RECOMMEND_SIZE.y - 1
        );
        LayoutHelper.BRightOfA(parametersScrollPanel,
                DesignerScreen.GAP + 2, commandStringSelectList,
                width - commandStringSelectList.getWidth() - DesignerScreen.GAP - 2,
                commandStringSelectList.getHeight() + DesignerScreen.GAP * 2 + 1 + TButton.RECOMMEND_SIZE.y);
        super.layout();
    }
}
