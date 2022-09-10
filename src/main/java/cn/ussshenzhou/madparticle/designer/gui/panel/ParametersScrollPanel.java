package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.util.Border;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {

    public ParametersScrollPanel() {
        super();
        this.setBorder(new Border(0xff00ff00, 1));
    }

    @Override
    public void layout() {

        super.layout();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, x, y, x + width - getScrollbarGap() - 6, y + height, 0x80000000);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

}
