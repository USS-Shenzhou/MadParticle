package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.designer.gui.panel.DesignerModePanel;
import cn.ussshenzhou.madparticle.designer.universal.screen.TScreen;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class DesignerScreen extends TScreen {
    public static final int GAP = 5;

    private final DesignerModePanel modePanel = new DesignerModePanel();

    public DesignerScreen() {
        super(new TranslatableComponent("gui.mp.designer.title"));
        this.add(modePanel);
    }

    @Override
    public void layout() {
        modePanel.setBounds(GAP, GAP,
                TButton.RECOMMEND_SIZE.x + modePanel.getModeSelector().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - GAP * 4 - 1 - 20);
        super.layout();
    }

    @Override
    public void tick() {
        super.tick();
        modePanel.tick();
    }

    @Override
    protected void renderBackGround(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, 0, 0, width, height, 0x80000000);
    }
}
