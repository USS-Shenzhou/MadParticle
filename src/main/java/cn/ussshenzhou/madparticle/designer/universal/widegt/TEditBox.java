package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.event.EditBoxFocusedEvent;
import cn.ussshenzhou.madparticle.designer.universal.util.EditBoxAccessorProxy;
import cn.ussshenzhou.madparticle.designer.universal.util.MWidget2TComponentHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author USS_Shenzhou
 */
public class TEditBox extends EditBox implements TWidget {
    TComponent parent = null;

    public TEditBox(Component tipText) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, tipText);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public TEditBox() {
        this(new TextComponent(""));
    }

    public int getCursorX() {
        return getX() + Minecraft.getInstance().font.width(getValue().substring(EditBoxAccessorProxy.getDisplayPos(this), getCursorPosition()));
    }

    public int getCurrentWordBeginX() {
        String s = getValue();
        int b = s.lastIndexOf(" ", getCursorPosition());
        if (b == getCursorPosition()) {
            b = s.lastIndexOf(" ", Mth.clamp(getCursorPosition() - 1, 0, Integer.MAX_VALUE));
        }
        b++;
        Font font = Minecraft.getInstance().font;
        return getX() + font.width(s.substring(EditBoxAccessorProxy.getDisplayPos(this), b)) + font.width(" ");
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
    public void setFocus(boolean pIsFocused) {
        if (pIsFocused) {
            MinecraftForge.EVENT_BUS.post(new EditBoxFocusedEvent(this));
        }
        super.setFocus(pIsFocused);
    }

    @SubscribeEvent
    public void onEditBoxFocused(EditBoxFocusedEvent event) {
        if (event.getWillFocused() != this) {
            this.setFocus(false);
        }
    }

    @Override
    public void onClose() {
        MinecraftForge.EVENT_BUS.unregister(this);
        TWidget.super.onClose();
    }

    @Override
    public void setParent(TComponent parent) {
        this.parent = parent;
    }

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
        return new Vec2i(this.width, 20);
    }

    @Override
    public Vec2i getSize() {
        return new Vec2i(width, height);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isInRange(pMouseX, pMouseY)) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (EditBoxAccessorProxy.isEdible(this)) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        } else {
            return false;
        }
    }
}
