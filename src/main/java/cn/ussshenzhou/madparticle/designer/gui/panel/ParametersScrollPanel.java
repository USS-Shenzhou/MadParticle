package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledEditBox;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSelectList;
import cn.ussshenzhou.madparticle.designer.universal.util.Border;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.TextComponent;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    TTitledButton tTitledButton = new TTitledButton(new TextComponent("titledButton"), new TextComponent("测试按钮"));
    TTitledEditBox tTitledEditBox = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledCycleButton<String> tTitledCycleButton = new TTitledCycleButton<>(new TextComponent("titledCycleButton"));

    public ParametersScrollPanel() {
        super();
        this.add(tTitledButton);
        this.add(tTitledEditBox);
        this.add(tTitledCycleButton);

        tTitledButton.getComponent().setOnPress(pButton -> {
            LogUtils.getLogger().debug("pressed A");
        });
        tTitledCycleButton.getComponent().addElement("1", stringTCycleButton -> {
            LogUtils.getLogger().debug("pressed " + stringTCycleButton.getSelected().getContent());
        });
        tTitledCycleButton.getComponent().addElement("2", stringTCycleButton -> {
            LogUtils.getLogger().debug("pressed " + stringTCycleButton.getSelected().getContent());
        });
        tTitledCycleButton.getComponent().addElement("3", stringTCycleButton -> {
            LogUtils.getLogger().debug("pressed " + stringTCycleButton.getSelected().getContent());
        });
        this.setBorder(new Border(0xffff0000,1));
        tTitledButton.setBorder(new Border(0xff00ff00,1));
        tTitledEditBox.setBorder(new Border(0xff00ff00,1));
        tTitledCycleButton.setBorder(new Border(0xff00ff00,1));
    }

    @Override
    public void layout() {
        tTitledButton.setBounds(5, 5, tTitledButton.getPreferredSize());
        LayoutHelper.BBottomOfA(tTitledEditBox, 5, tTitledButton, 100, tTitledEditBox.getPreferredSize().y);
        LayoutHelper.BBottomOfA(tTitledCycleButton, 5, tTitledEditBox, tTitledButton.getPreferredSize());
        super.layout();
    }

}
