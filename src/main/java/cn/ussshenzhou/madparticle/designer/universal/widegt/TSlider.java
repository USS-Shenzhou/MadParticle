package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.AccessorProxy;
import cn.ussshenzhou.madparticle.designer.universal.util.MWidget2TComponentHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TSlider extends SliderButton implements TWidget {
    private boolean visible = true;
    TComponent parent = null;

    public TSlider(Options pOptions, int pX, int pY, int pWidth, int pHeight, ProgressOption pProgressOption, List<FormattedCharSequence> pTooltip) {
        super(pOptions, pX, pY, pWidth, pHeight, pProgressOption, pTooltip);
    }

    public TSlider(double pMinValue, double pMaxValue, float pSteps, Component tipText) {
        this(Minecraft.getInstance().options, 0, 0, 0, 0,
                new ProgressOption("t88.tslider", pMinValue, pMaxValue, pSteps, o -> 0d, (o, d) -> {
                }, (o, p) -> tipText), ImmutableList.of());

        ProgressOption progressOption = new ProgressOption("t88.tslider", pMinValue, pMaxValue, pSteps,
                (o) -> value, (o, d) -> value = d, (o, p) -> tipText
        );
        //Function<Minecraft, List<FormattedCharSequence>> pTooltipSupplier = m -> ImmutableList.of();
        AccessorProxy.SliderProxy.setOption(this, progressOption);
        //AccessorProxy.SliderProxy.setToolTip(this, pTooltipSupplier.apply(Minecraft.getInstance()));
        this.updateMessage();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isInRange(pMouseX, pMouseY, 2, 2)) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return false;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            //modified for compatibility with TScrollPanel
            double y = getParentScrollAmountIfExist() + pMouseY;
            this.isHovered = pMouseX >= this.x && y >= this.y && pMouseX < this.x + this.width && y < this.y + this.height;
            this.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
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
    public void setBounds(int x, int y, int width, int height) {
        MWidget2TComponentHelper.setBounds(x, y, width, height, this);
    }

    @Override
    public void setAbsBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setParent(TComponent parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public TComponent getParent() {
        return parent;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public Vec2i getPreferredSize() {
        return new Vec2i(width, 20);
    }

    @Override
    public Vec2i getSize() {
        return new Vec2i(width, height);
    }

    @Override
    public void tick() {
    }

}
