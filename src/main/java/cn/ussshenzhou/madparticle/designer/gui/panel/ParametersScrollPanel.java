package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    protected final TCycleButton<String> cycleButton = new TCycleButton<>();

    public ParametersScrollPanel() {
        super();
        this.add(cycleButton);
        cycleButton.addElement("test1");
        cycleButton.addElement("test2");
        cycleButton.addElement("test3");
    }

    @Override
    public void layout() {
        cycleButton.setBounds(5, 5, TButton.RECOMMEND_SIZE);
        super.layout();
    }

}
