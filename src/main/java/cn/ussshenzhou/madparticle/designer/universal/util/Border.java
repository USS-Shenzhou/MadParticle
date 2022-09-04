package cn.ussshenzhou.madparticle.designer.universal.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

/**
 * @author USS_Shenzhou
 */
public class Border {
    private int color;
    private int thickness;

    public Border(int color, int thickness) {
        this.color = color;
        this.thickness = thickness;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public static void renderBorder(PoseStack pPoseStack, int color, int thickness, int x, int y, int width, int height) {
        GuiComponent.fill(pPoseStack, x - thickness, y - thickness, x + width + thickness, y, color);
        GuiComponent.fill(pPoseStack, x - thickness, y + height, x + width + thickness, y + height + thickness, color);
        GuiComponent.fill(pPoseStack, x - thickness, y, x, y + height, color);
        GuiComponent.fill(pPoseStack, x + width, y, x + width + thickness, y + height, color);
    }
}
