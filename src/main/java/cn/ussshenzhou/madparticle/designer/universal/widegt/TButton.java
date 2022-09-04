package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.MWidget2TComponentHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Size;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class TButton extends Button implements TWidget {
    public static final Size RECOMMEND_SIZE = new Size(52, 20);
    private boolean visible = true;
    TComponent parent = null;
    private OnPress onPress;

    public TButton(Component pMessage) {
        super(0, 0, 0, 0, pMessage, button -> {
        });
        this.onPress = pButton -> {
        };
    }

    public TButton(Component pMessage, OnPress pOnPress) {
        super(0, 0, 0, 0, pMessage, button -> {
        });
        this.onPress = pOnPress;
    }

    public TButton(Component pMessage, OnPress pOnPress, OnTooltip pOnTooltip) {
        super(0, 0, 0, 0, pMessage, button -> {
        }, pOnTooltip);
        this.onPress = pOnPress;
    }

    public void setOnPress(OnPress onPress) {
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
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
    public void setParent(TComponent parent) {
        this.parent = parent;
    }

    @Override
    public TComponent getParent() {
        return this.parent;
    }

    @Override
    public Size getPreferredSize() {
        return new Size(this.width, 20);
    }

    @Override
    public Size getSize() {
        return new Size(width, height);
    }

    @Override
    public void tick() {

    }
}
