package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.widegt.CommandStringSelectList;
import cn.ussshenzhou.madparticle.designer.universal.advanced.TSuggestedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class HelperModePanel extends TPanel {
    private final TButton copy = new TButton(new TranslatableComponent("gui.mp.de.helper.copy"));
    private final TSuggestedEditBox command = new TSuggestedEditBox(MadParticleCommand::new);
    private final CommandStringSelectList commandStringSelectList = new CommandStringSelectList();

    private ParametersScrollPanel parametersScrollPanel = null;

    public HelperModePanel() {
        super();
        command.getEditBox().setFocus(false);
        command.getEditBox().setMaxLength(32500);
        this.add(copy);
        this.add(command);
        this.add(commandStringSelectList);

        copy.setOnPress(pButton -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(command.getEditBox().getValue());
            //TODO: copied!
        });
    }

    public void setParametersScrollPanel(ParametersScrollPanel parametersScrollPanel) {
        if (this.parametersScrollPanel != null) {
            this.parametersScrollPanel.setVisibleT(false);
            this.remove(this.parametersScrollPanel);
        }
        this.parametersScrollPanel = parametersScrollPanel;
        if (parametersScrollPanel!=null){
            this.add(parametersScrollPanel);
            parametersScrollPanel.setVisibleT(true);
        }
        layout();
    }

    @Override
    public void layout() {
        copy.setBounds(width - TButton.RECOMMEND_SIZE.x, (40 - TButton.RECOMMEND_SIZE.y) / 2);
        LayoutHelper.BLeftOfA(command, DesignerScreen.GAP, copy, width - copy.getWidth() - DesignerScreen.GAP, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BBottomOfA(commandStringSelectList, DesignerScreen.GAP, command,
                TButton.RECOMMEND_SIZE.x + commandStringSelectList.getComponent().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - command.getY() - command.getHeight() - DesignerScreen.GAP * 2 - TButton.RECOMMEND_SIZE.y - 1
        );
        if (parametersScrollPanel != null) {
            LayoutHelper.BRightOfA(parametersScrollPanel,
                    DesignerScreen.GAP + 2, commandStringSelectList,
                    width - commandStringSelectList.getWidth() - DesignerScreen.GAP - 2,
                    commandStringSelectList.getHeight() + DesignerScreen.GAP * 2 + 1 + TButton.RECOMMEND_SIZE.y);
        }
        super.layout();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
