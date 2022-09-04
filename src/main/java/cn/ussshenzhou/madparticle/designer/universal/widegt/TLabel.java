package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

/**
 * @author USS_Shenzhou
 */
public class TLabel extends TComponent {
    private final String text;
    Font font = Minecraft.getInstance().font;
    private int size = 7;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

    public TLabel(String s) {
        this.text = s;
    }

    public TLabel(String s, int foreground) {
        this(s);
        this.setForeground(foreground);
    }

    public void setFontSize(int size) {
        this.size = size;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.pushPose();
        float scaleFactor = size / 7f;
        pPoseStack.scale(scaleFactor, scaleFactor, 1);
        drawString(pPoseStack, font, text,
                getAlignedX(),
                (int) ((y + (height - size) / 2) / scaleFactor),
                foreground);
        pPoseStack.popPose();
    }

    private int getAlignedX() {
        int textWidth = getPreferredSize().x;
        if (width == textWidth) {
            return x;
        }
        switch (horizontalAlignment) {
            case CENTER:
                return x + (width - textWidth) / 2;
            case RIGHT:
                return x + width - textWidth;
            default:
                return x;
        }
    }

    @Override
    public Size getPreferredSize() {
        return new Size(font.width(text), font.lineHeight);
    }

}
