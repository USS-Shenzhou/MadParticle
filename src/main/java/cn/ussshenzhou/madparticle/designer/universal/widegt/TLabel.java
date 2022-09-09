package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/**
 * @author USS_Shenzhou
 */
public class TLabel extends TComponent {
    private Component text;
    private int size = 7;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

    Font font = Minecraft.getInstance().font;

    public TLabel() {
        this.text = new TextComponent("");
    }

    public TLabel(Component s) {
        this.text = s;
    }

    public TLabel(Component s, int foreground) {
        this(s);
        this.setForeground(foreground);
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public void setFontSize(int size) {
        this.size = size;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    public void layout() {

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
    public Vec2i getPreferredSize() {
        return new Vec2i(font.width(text), font.lineHeight);
    }

}
