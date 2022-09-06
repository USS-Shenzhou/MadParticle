package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class DesignerModeSelectList extends TTitledSelectList<DesignerModeSelectList.DesignerMode> {

    public DesignerModeSelectList() {
        super(new TranslatableComponent("gui.mp.de.mode.title"));
        this.addElement(DesignerMode.HELPER, list -> {
            if (DesignerScreen.getInstance() != null) {
                DesignerScreen.getInstance().setVisibleMode(DesignerMode.HELPER);
            }
        });
        this.addElement(DesignerMode.LINE, list -> {
            assert DesignerScreen.getInstance() != null;
            DesignerScreen.getInstance().setVisibleMode(DesignerMode.LINE);
        });
        this.getList().setSelected(0);
        this.setLabelHeight(16);
    }

    @SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
    public enum DesignerMode {
        HELPER("gui.mp.de.mode.helper"),
        LINE("gui.mp.de.mode.line");

        private final String translateKey;

        private DesignerMode(String translateKey) {
            this.translateKey = translateKey;
        }

        @Override
        public String toString() {
            return this.translateKey;
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        //This is a bad example. You should use panels instead of direct fill() to draw split lines.
        fill(pPoseStack, x - 1, y + height + DesignerScreen.GAP, x + width + 1, y + height + 1 + DesignerScreen.GAP, 0x80ffffff);
        fill(pPoseStack, x + width, y, x + width + 1, y + height+ DesignerScreen.GAP, 0x80ffffff);
    }
}
