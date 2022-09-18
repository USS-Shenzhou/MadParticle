package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    public static final Vec2i EDITBOX_SIZE = new Vec2i(35, 36);

    public final TTitledEditBox target = new TTitledEditBox(new TranslatableComponent("gui.mp.de.helper.target"));


    public ParametersScrollPanel() {
        super();
    }

    @Override
    public void layout() {
        int gap = 5;
        Vec2i stdTitledEditBox = new Vec2i(
                (width - TButton.RECOMMEND_SIZE.x - 7 * gap) / 7,
                36
        );


        super.layout();
    }

}
