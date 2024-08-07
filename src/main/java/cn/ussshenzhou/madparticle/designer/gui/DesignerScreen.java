package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.designer.gui.panel.HelperModePanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.SettingPanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.TadaModePanel;
import cn.ussshenzhou.madparticle.designer.gui.widegt.DesignerModeSelectList;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import cn.ussshenzhou.t88.gui.widegt.TSelectList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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
    private final SettingPanel settingPanel = new SettingPanel();
    private final TadaModePanel tadaModePanel = new TadaModePanel();

    private DesignerScreen() {
        super(Component.translatable("gui.mp.designer.title"));
        this.add(designerModeSelectList);
        this.add(helperModePanel);
        this.add(settingPanel);
        if (designerScreen == null) {
            designerScreen = this;
        }
        this.add(tadaModePanel);
    }

    public static @Nullable DesignerScreen getInstance() {
        return designerScreen;
    }

    public static DesignerScreen newInstance() {
        if (designerScreen != null) {
            designerScreen.onClose(true);
        }
        designerScreen = new DesignerScreen();
        return designerScreen;
    }

    public void initFromCommand(String command) {
        this.helperModePanel.getCommandEditBox().getEditBox().setValue(command);
        this.helperModePanel.unwrap();
    }

    @Override
    public void layout() {
        designerModeSelectList.setBounds(GAP, GAP,
                TButton.RECOMMEND_SIZE.x + designerModeSelectList.getComponent().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - GAP * 4 - 1 - 20);
        LayoutHelper.BRightOfA(helperModePanel, GAP + 1, designerModeSelectList,
                width - designerModeSelectList.getWidth() - 3 * GAP - 1,
                height - 2 * GAP);
        LayoutHelper.BSameAsA(settingPanel, helperModePanel);
        LayoutHelper.BSameAsA(tadaModePanel, helperModePanel);
        super.layout();
        designerModeSelectList.getComponent().setSelected(designerModeSelectList.getComponent().getSelected());
    }

    public void setVisibleMode(DesignerModeSelectList.DesignerMode mode) {
        TPanel[] panels = {helperModePanel, settingPanel, tadaModePanel};
        Arrays.stream(panels).forEach(p -> p.setVisibleT(false));
        switch (mode) {
            case HELPER -> helperModePanel.setVisibleT(true);
            case SETTING -> settingPanel.setVisibleT(true);
            case TADA -> tadaModePanel.setVisibleT(true);
            default -> {
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        /*designerModeSelectList.tickT();
        helperModePanel.tickT();
        lineModePanel.tickT();
        settingPanel.tickT();*/
    }

    public DesignerModeSelectList getDesignerModeSelectList() {
        return designerModeSelectList;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256 && this.shouldCloseOnEsc()) {
            this.onClose(false);
            return true;
        } else {
            return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }
}
