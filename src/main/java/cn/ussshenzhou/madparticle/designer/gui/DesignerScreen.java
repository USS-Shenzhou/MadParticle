package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.designer.gui.panel.HelperModePanel;
import cn.ussshenzhou.madparticle.designer.gui.panel.SettingPanel;
import cn.ussshenzhou.madparticle.util.CameraHelper;
import cn.ussshenzhou.t88.gui.container.TTabPageContainer;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * @author USS_Shenzhou
 */
public class DesignerScreen extends TScreen {
    public static final int GAP = 4;
    private static DesignerScreen designerScreen = null;

    private final TTabPageContainer tabPageContainer = new TTabPageContainer();

    private final HelperModePanel helperModePanel = new HelperModePanel();
    private final SettingPanel settingPanel = new SettingPanel();
    //FIXME TADA private final TadaModePanel tadaModePanel = new TadaModePanel();
    private CameraType prevCameraType;

    private DesignerScreen(CameraType prevCameraType) {
        super(Component.translatable("gui.mp.designer.title"));
        tabPageContainer.newTab(Component.translatable("gui.mp.de.mode.helper"), helperModePanel).setCloseable(false);
        //FIXME TADA tabPageContainer.newTab(Component.translatable("gui.mp.de.mode.tada"), tadaModePanel).setCloseable(false);
        tabPageContainer.newTab(Component.translatable("gui.mp.de.mode.setting"), settingPanel).setCloseable(false);
        this.add(tabPageContainer);

        if (designerScreen == null) {
            designerScreen = this;
        }
        this.prevCameraType = prevCameraType;
    }

    public static @Nullable DesignerScreen getInstance(CameraType prevCameraType) {
        if (designerScreen != null) {
            designerScreen.prevCameraType = prevCameraType;
        }
        return designerScreen;
    }

    public static DesignerScreen newInstance(CameraType prevCameraType) {
        if (designerScreen != null) {
            designerScreen.onClose(true);
        }
        designerScreen = new DesignerScreen(prevCameraType);
        return designerScreen;
    }

    public void initFromCommand(String command) {
        this.helperModePanel.getCommandEditBox().getEditBox().setValue(command);
        this.helperModePanel.unwrap();
    }

    @Override
    public void layout() {
        tabPageContainer.setBounds(0, 0, width, height);
        super.layout();
    }

    public TTabPageContainer getTabPageContainer() {
        return tabPageContainer;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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

    @Override
    public boolean isPauseScreen() {
        return tabPageContainer.getSelectedTab() == tabPageContainer.getTabs().getLast();
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.mouseGrabbed) {
            var x = mc.getWindow().getScreenWidth() / 2;
            var y = mc.getWindow().getScreenHeight() / 2;
            super.render(graphics, x, y, pPartialTick);
        } else {
            super.render(graphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.mouseGrabbed) {
            var x = mc.getWindow().getScreenWidth() / 2;
            var y = mc.getWindow().getScreenHeight() / 2;
            return super.mouseClicked(x, y, pButton);
        } else {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.mouseGrabbed) {
            var x = mc.getWindow().getScreenWidth() / 2;
            var y = mc.getWindow().getScreenHeight() / 2;
            return super.mouseReleased(x, y, pButton);
        } else {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.mouseGrabbed) {
            var x = mc.getWindow().getScreenWidth() / 2;
            var y = mc.getWindow().getScreenHeight() / 2;
            return super.mouseDragged(x, y, pButton, pDragX, pDragY);
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double deltaX, double deltaY) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.mouseGrabbed) {
            var x = mc.getWindow().getScreenWidth() / 2;
            var y = mc.getWindow().getScreenHeight() / 2;
            return super.mouseScrolled(x, y, deltaX, deltaY);
        } else {
            return super.mouseScrolled(pMouseX, pMouseY, deltaX, deltaY);
        }
    }

    @Override
    public void onClose(boolean isFinal) {
        super.onClose(isFinal);
        CameraHelper.setCameraType(prevCameraType);
    }
}
