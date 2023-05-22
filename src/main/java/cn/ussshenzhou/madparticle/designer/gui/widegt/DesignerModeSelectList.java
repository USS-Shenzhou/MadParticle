package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.t88.gui.combine.TTitledSelectList;
import cn.ussshenzhou.t88.gui.widegt.TSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class DesignerModeSelectList extends TTitledSelectList<DesignerModeSelectList.DesignerMode> {

    public DesignerModeSelectList() {
        super(Component.translatable("gui.mp.de.mode.title"), new TSelectList<>());
        this.addElement(DesignerMode.HELPER, list -> {
            if (DesignerScreen.getInstance() != null) {
                DesignerScreen.getInstance().setVisibleMode(DesignerMode.HELPER);
            }
        });
        /*this.addElement(DesignerMode.LINE, list -> {
            if (DesignerScreen.getInstance() != null) {
                DesignerScreen.getInstance().setVisibleMode(DesignerMode.LINE);
            }
        });*/
        this.addElement(DesignerMode.SETTING, list -> {
            if (DesignerScreen.getInstance() != null) {
                DesignerScreen.getInstance().setVisibleMode(DesignerMode.SETTING);
            }
        });
        this.getComponent().setSelected(0);
    }

    @SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
    public enum DesignerMode {
        HELPER("gui.mp.de.mode.helper"),
        //LINE("gui.mp.de.mode.line"),
        SETTING("gui.mp.de.mode.setting");

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
        fill(pPoseStack, x + width, y, x + width + 1, y + height + DesignerScreen.GAP, 0x80ffffff);
    }
}
