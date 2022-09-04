package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.MWidget2TComponentHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class TEditBox extends EditBox implements TWidget {
    TComponent parent = null;

    public TEditBox(Component tipText) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, tipText);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        MWidget2TComponentHelper.setBounds(x, y, width, height, this);
    }

    @Override
    public void setParent(TComponent parent) {
        this.parent= parent;
    }

    @Override
    public TComponent getParent() {
        return parent;
    }

    @Override
    public Size getPreferredSize() {
        return new Size(this.width, 20);
    }
}
