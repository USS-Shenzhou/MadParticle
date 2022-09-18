package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.Border;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.TextComponent;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    public static final Vec2i EDITBOX_SIZE = new Vec2i(35,36);

    TTitledButton tTitledButton = new TTitledButton(new TextComponent("titledButton"), new TextComponent("测试按钮"));
    TTitledEditBox tTitledEditBox1 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox2 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox3 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox4 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox5 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox6 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledEditBox tTitledEditBox7 = new TTitledEditBox(new TextComponent("测试输入框"));
    TTitledCycleButton<String> tTitledCycleButton = new TTitledCycleButton<>(new TextComponent("titledCycleButton"));

    public ParametersScrollPanel() {
        super();
        this.add(tTitledButton);
        this.add(tTitledEditBox1);
        this.add(tTitledEditBox2);
        this.add(tTitledEditBox3);
        this.add(tTitledEditBox4);
        this.add(tTitledEditBox5);
        this.add(tTitledEditBox6);
        this.add(tTitledEditBox7);
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
        tTitledCycleButton.setBorder(new Border(0xff00ff00,1));
    }

    @Override
    public void layout() {
        tTitledButton.setBounds(5, 5, tTitledButton.getPreferredSize());
        LayoutHelper.BRightOfA(tTitledEditBox1, 5, tTitledButton, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox2, 5, tTitledEditBox1, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox3, 5, tTitledEditBox2, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox4, 5, tTitledEditBox3, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox5, 5, tTitledEditBox4, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox6, 5, tTitledEditBox5, EDITBOX_SIZE);
        LayoutHelper.BRightOfA(tTitledEditBox7, 5, tTitledEditBox6, EDITBOX_SIZE);
        LayoutHelper.BBottomOfA(tTitledCycleButton, 5, tTitledEditBox1, tTitledButton.getPreferredSize());
        super.layout();
    }

}
