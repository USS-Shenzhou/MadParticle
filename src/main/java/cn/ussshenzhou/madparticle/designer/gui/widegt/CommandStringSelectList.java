package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSelectList;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class CommandStringSelectList extends TTitledSelectList<CommandStringSelectList.SubCommand> {
    private final TButton newCommand = new TButton(new TranslatableComponent("gui.mp.de.helper.new"));
    private final TButton delete = new TButton(new TranslatableComponent("gui.mp.de.helper.delete"));

    public CommandStringSelectList() {
        super(new TranslatableComponent("gui.mp.de.helper.command_string"));
        this.setLabelHeight(16);
        this.add(newCommand);
        this.add(delete);

        newCommand.setOnPress(pButton -> {
            list.addElement(new SubCommand());
        });
        delete.setOnPress(pButton -> {
            list.removeElement(list.getSelected());
        });
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

    public class SubCommand {

        @Override
        public String toString() {
            return "null";
        }
    }
}
