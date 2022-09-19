package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    public static final Vec2i EDITBOX_SIZE = new Vec2i(35, 36);
    public static final Vec2i BUTTON_SIZE = TButton.RECOMMEND_SIZE;

    public final TTitledEditBox target = new TTitledEditBox(new TranslatableComponent("gui.mp.de.helper.target"));
    public final TButton tryDefault = new TButton(new TranslatableComponent("gui.mp.de.helper.try_default"));{
        tryDefault.setOnPress(pButton -> {
            //TODO
        });
    }

    public ParametersScrollPanel() {
        super();
        this.add(target);
        this.add(tryDefault);
    }

    @Override
    public void layout() {
        int gap = 5;
        Vec2i stdTitledEditBox = new Vec2i(
                (getUsableWidth() - TButton.RECOMMEND_SIZE.x - 7 * gap) / 7,
                36
        );
        target.setBounds(gap, gap, getUsableWidth() - gap * 3 - BUTTON_SIZE.x * 2, EDITBOX_SIZE.y);
        LayoutHelper.BRightOfA(tryDefault, gap, target, BUTTON_SIZE.x * 2, BUTTON_SIZE.y);
        LayoutHelper.BBottomOfA(tryDefault, -4, tryDefault);

        super.layout();
    }

}
