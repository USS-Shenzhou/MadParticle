package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.designer.universal.screen.TScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class DesignerScreen extends TScreen {

    public DesignerScreen() {
        super(new TranslatableComponent("gui.mp.designer.title"));
    }

    @Override
    protected void layout(int width, int height) {
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void renderBackGround(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, 0, 0, width, height, 0x80000000);
    }
}
