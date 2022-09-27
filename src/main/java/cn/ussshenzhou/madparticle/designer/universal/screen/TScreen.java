package cn.ussshenzhou.madparticle.designer.universal.screen;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TComponent;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;

/**
 * @author USS_Shenzhou
 */
public abstract class TScreen extends Screen {
    private boolean needRelayout = true;
    LinkedList<TWidget> tChildren = new LinkedList<>();

    protected TScreen(Component pTitle) {
        super(pTitle);
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
        needRelayout = true;
    }

    @Override
    public void tick() {
        if (needRelayout) {
            layout();
            needRelayout = false;
        }
        super.tick();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        renderBackGround(pPoseStack, pMouseX, pMouseY, pPartialTick);
        LinkedList<Widget> renderTop = new LinkedList<>();
        for (TWidget w : this.tChildren) {
            if (w.isVisible()) {
                w.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
        for (TWidget w : this.tChildren) {
            if (w.isVisible()) {
                w.renderTop(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    public void add(TWidget tWidget) {
        tChildren.add(tWidget);
    }

    public void remove(TWidget tWidget) {
        tChildren.remove(tWidget);
    }


    public void layout() {
        for (TWidget w : this.tChildren) {
            if (w instanceof TComponent t) {
                t.layout();
            }
        }
    }

    protected void renderBackGround(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (TWidget tWidget : tChildren) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.mouseClicked(pMouseX, pMouseY, pButton)) {
                this.setFocused(tWidget);
                if (pButton == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.setDragging(false);
        for (TWidget tWidget : tChildren) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        for (TWidget tWidget : tChildren) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        for (TWidget tWidget : tChildren) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.mouseScrolled(pMouseX, pMouseY, pDelta)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256 && this.shouldCloseOnEsc()) {
            this.onClose(true);
            return true;
        } else {
            return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return this.getFocused() != null && this.getFocused().keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }

    @Override
    public void onClose() {
        onClose(true);
    }

    public void onClose(boolean isFinal) {
        if (isFinal) {
            tChildren.forEach(TWidget::onClose);
        }
        super.onClose();
    }
}
