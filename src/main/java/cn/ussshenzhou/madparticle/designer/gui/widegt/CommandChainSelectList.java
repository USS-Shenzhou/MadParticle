package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.panel.HelperModePanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.ParametersPanel;
import cn.ussshenzhou.t88.gui.advanced.TConstrainedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledSelectList;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.util.MouseHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TSelectList;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class CommandChainSelectList extends TTitledSelectList<CommandChainSelectList.SubCommand> {
    public final TButton newCommand = new TButton(Component.translatable("gui.mp.de.helper.new"));
    public final TButton delete = new TButton(Component.translatable("gui.mp.de.helper.delete"));

    public CommandChainSelectList() {
        super(Component.translatable("gui.mp.de.helper.command_chain"), new TSelectList<>());
        this.add(newCommand);
        this.add(delete);
        initButton();
        delete.setOnPress(_ -> {
            getComponent().removeElement(getComponent().getSelected());
            delete.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(null);
            this.checkChild();
        });
    }

    protected void initButton() {
        newCommand.setOnPress(_ -> {
            var list = getComponent();
            var sub = new CommandChainSelectList.SubCommand();
            addElement(sub, list1 -> {
                list1.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().parametersPanel);
            });
            if (list.getSelected() == null) {
                list.setSelected(list.children().get(list.children().size() - 1));
            }
            this.checkChild();
        });
        delete.setOnPress(_ -> {
            getComponent().removeElement(getComponent().getSelected());
            delete.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(null);
            this.checkChild();
        });
    }

    public void checkChild() {
        var list = this.getComponent().children();
        for (int i = 0; i < list.size(); i++) {
            var panel = list.get(i).getContent().parametersPanel;
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
        LayoutHelper.BBottomOfA(newCommand, DesignerScreen.GAP, this, TButton.RECOMMEND_SIZE.x, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BBottomOfA(delete, DesignerScreen.GAP, newCommand, TButton.RECOMMEND_SIZE.x, TButton.RECOMMEND_SIZE.y);
        super.layout();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (isInRange(MouseHelper.getMouseX(), MouseHelper.getMouseY(), 4, 4)) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }

    public SubCommand addSubCommand(ParametersPanel parametersPanel) {
        return new SubCommand(parametersPanel);
    }

    public static class SubCommand {
        public final ParametersPanel parametersPanel;

        public SubCommand() {
            this(new ParametersPanel());
        }

        public SubCommand(ParametersPanel parametersPanel) {
            this.parametersPanel = parametersPanel;
        }

        @Override
        public String toString() {
            String value = parametersPanel.target.getComponent().getEditBox().getValue();
            if (value.isEmpty()) {
                return "null";
            }
            String[] s = value.split(":");
            return s[s.length - 1];
        }

        public ParametersPanel getParametersScrollPanel() {
            return parametersPanel;
        }
    }

    public String warp() {
        StringBuilder builder = new StringBuilder("mp");
        Iterator<TSelectList<SubCommand>.Entry> iterator = this.getComponent().children().iterator();
        while (iterator.hasNext()) {
            var subCommand = iterator.next();
            String sub = subCommand.getContent().parametersPanel.wrap();
            Thread.startVirtualThread(() -> checkWrapped(subCommand, sub));
            builder.append(sub);
            if (iterator.hasNext()) {
                builder.append(" expireThen");
            }
        }
        return builder.toString();
    }

    private void checkWrapped(TSelectList<?>.Entry entry, String subCommand) {
        subCommand = "mp" + subCommand;
        subCommand = subCommand.replace("=", "0");
        ParseResults<CommandSourceStack> parseResults = MadParticleCommand.justParse(subCommand);
        Map<?, CommandSyntaxException> map = parseResults.getExceptions();
        if ((!map.isEmpty()) || parseResults.getContext().build(subCommand).getNodes().isEmpty()) {
            entry.setSpecialForeground(TConstrainedEditBox.RED_TEXT_COLOR);
        } else {
            entry.clearSpecialForeground();
        }
    }
}
