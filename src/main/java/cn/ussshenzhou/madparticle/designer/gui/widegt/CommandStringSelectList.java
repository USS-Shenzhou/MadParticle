package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.panel.HelperModePanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.ParametersScrollPanel;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSelectList;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.MouseHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class CommandStringSelectList extends TTitledSelectList<CommandStringSelectList.SubCommand> {
    private final TButton newCommand = new TButton(new TranslatableComponent("gui.mp.de.helper.new"));
    private final TButton delete = new TButton(new TranslatableComponent("gui.mp.de.helper.delete"));

    public CommandStringSelectList() {
        super(new TranslatableComponent("gui.mp.de.helper.command_string"), new TSelectList<>());
        this.add(newCommand);
        this.add(delete);

        newCommand.setOnPress(pButton -> {
            var list = getComponent();
            addElement(new SubCommand(), list1 -> {
                this.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().parametersScrollPanel);
            });
            if (list.getSelected() == null) {
                list.setSelected(list.children().get(list.children().size() - 1));
            }
            this.checkChild();
        });
        delete.setOnPress(pButton -> {
            getComponent().removeElement(getComponent().getSelected());
            this.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(null);
            this.checkChild();
        });
    }

    private void checkChild() {
        var list = this.getComponent().children();
        for (int i = 0; i < list.size(); i++) {
            var panel = list.get(i).getContent().parametersScrollPanel;
            if (i == 0) {
                if (panel.isChild()) {
                    panel.setChild(false);
                }
            } else {
                if (!panel.isChild()) {
                    panel.setChild(true);
                }
            }
        }
    }

    @Override
    public void layout() {
        LayoutHelper.BBottomOfA(newCommand, DesignerScreen.GAP * 2 + 1, DesignerScreen.getInstance().getDesignerModeSelectList(),
                TButton.RECOMMEND_SIZE.x, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BBottomOfA(delete, DesignerScreen.GAP * 2 + 1,
                this, TButton.RECOMMEND_SIZE.x, TButton.RECOMMEND_SIZE.y);
        super.layout();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        //This is a bad example. You should use panels instead of direct fill() to draw split lines.
        fill(pPoseStack, x + width,
                y,
                x + width + 1,
                delete.y + delete.getHeight(),
                0x80ffffff
        );
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (isInRange(MouseHelper.getMouseX(), MouseHelper.getMouseY(), 4, 4)) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }

    public class SubCommand {
        private final ParametersScrollPanel parametersScrollPanel;

        public SubCommand() {
            parametersScrollPanel = new ParametersScrollPanel();
        }

        @Override
        public String toString() {
            String value = parametersScrollPanel.target.getComponent().getEditBox().getValue();
            if (value.isEmpty()) {
                return "null";
            }
            String[] s = value.split(":");
            return s[s.length - 1];
        }
    }
}
