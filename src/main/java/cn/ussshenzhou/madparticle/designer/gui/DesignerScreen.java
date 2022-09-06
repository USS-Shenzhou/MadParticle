package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.designer.gui.panel.HelperModePanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.LineModePanel;
import cn.ussshenzhou.madparticle.designer.gui.widegt.DesignerModeSelectList;
import cn.ussshenzhou.madparticle.designer.universal.screen.TScreen;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author USS_Shenzhou
 */
public class DesignerScreen extends TScreen {
    public static final int GAP = 5;
    private static DesignerScreen designerScreen = null;

    private final DesignerModeSelectList designerModeSelectList = new DesignerModeSelectList();
    private final HelperModePanel helperModePanel = new HelperModePanel();
    private final LineModePanel lineModePanel = new LineModePanel();

    public DesignerScreen() {
        super(new TranslatableComponent("gui.mp.designer.title"));
        this.add(designerModeSelectList);
        this.add(helperModePanel);
        this.add(lineModePanel);
        if (designerScreen == null) {
            designerScreen = this;
        }
    }

    public static @Nullable DesignerScreen getInstance() {
        return designerScreen;
    }

    public static DesignerScreen newInstance() {
        designerScreen = new DesignerScreen();
        return designerScreen;
    }

    @Override
    public void layout() {
        designerModeSelectList.setBounds(GAP, GAP,
                TButton.RECOMMEND_SIZE.x + designerModeSelectList.getList().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - GAP * 4 - 1 - 20);
        LayoutHelper.BRightOfA(helperModePanel, GAP + 1, designerModeSelectList,
                width - designerModeSelectList.getWidth() - 3 * GAP - 1,
                height - 2 * GAP);
        LayoutHelper.BSameAsA(lineModePanel, helperModePanel);
        super.layout();
    }

    @SuppressWarnings("AlibabaSwitchStatement")
    public void setVisibleMode(DesignerModeSelectList.DesignerMode mode) {
        TPanel[] panels = {helperModePanel, lineModePanel};
        Arrays.stream(panels).forEach(p -> p.setVisible(false));
        switch (mode) {
            case HELPER -> helperModePanel.setVisible(true);
            case LINE -> lineModePanel.setVisible(true);
            default -> {
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        designerModeSelectList.tick();
        helperModePanel.tick();
        lineModePanel.tick();
    }

    @Override
    protected void renderBackGround(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, 0, 0, width, height, 0x80000000);
    }

    public DesignerModeSelectList getDesignerModeSelectList() {
        return designerModeSelectList;
    }
}