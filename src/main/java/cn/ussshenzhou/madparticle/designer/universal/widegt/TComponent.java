package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.Border;
import cn.ussshenzhou.madparticle.designer.universal.util.Size;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

import java.util.LinkedList;

/**
 * @author USS_Shenzhou
 */
public abstract class TComponent extends GuiComponent implements TWidget {
    protected int x, y, width, height;
    protected int relativeX, relativeY;
    boolean visible = true;
    //argb
    int background = 0x00000000;
    int foreground = 0xffffffff;
    LinkedList<TWidget> children = new LinkedList<>();
    Border border = null;
    TComponent parent = null;

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.relativeX = x;
        this.relativeY = y;
        if (parent != null) {
            this.x = x + parent.x;
            this.y = y + parent.y;
        } else {
            this.x = x;
            this.y = y;
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public void setAbsBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void layout() {
        for (TWidget tWidget : children) {
            if (tWidget instanceof TComponent tComponent) {
                tComponent.layout();
            }
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (border != null) {
            renderBorder(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        renderBackground(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderChildren(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private void renderBorder(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int thickness = border.getThickness();
        int color = border.getColor();
        fill(pPoseStack, x - thickness, y - thickness, x + width + thickness, y, color);
        fill(pPoseStack, x - thickness, y + height, x + width + thickness, y + height + thickness, color);
        fill(pPoseStack, x - thickness, y, x, y + height, color);
        fill(pPoseStack, x + width, y, x + width + thickness, y + height, color);
    }

    private void renderBackground(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, x, y, x + width, y + height, background);
    }

    private void renderChildren(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        for (TWidget tWidget : children) {
            if (tWidget.isVisible()) {
                tWidget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    @Override
    public void tick() {
        for (TWidget tWidget : children) {
            tWidget.tick();
        }
    }

    @Override
    public Size getPreferredSize() {
        return new Size(width, height);
    }

    @Override
    public Size getSize() {
        return new Size(width, height);
    }

    public void add(TWidget child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (TWidget tWidget : children) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (TWidget tWidget : children) {
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
        for (TWidget tWidget : children) {
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
        for (TWidget tWidget : children) {
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
        for (TWidget tWidget : children) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        for (TWidget tWidget : children) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.keyReleased(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        for (TWidget tWidget : children) {
            if (!tWidget.isVisible()) {
                continue;
            }
            if (tWidget.charTyped(pCodePoint, pModifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TComponent getParent() {
        return parent;
    }

    @Override
    public void setParent(TComponent parent) {
        this.parent = parent;
    }

    public void remove(TWidget tWidget) {
        children.remove(tWidget);
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getForeground() {
        return foreground;
    }

    public void setForeground(int foreground) {
        this.foreground = foreground;
    }
}
