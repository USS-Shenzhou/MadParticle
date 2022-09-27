package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.universal.util.Border;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;

public class ParticlePreviewPanel extends TPanel {
    public ParticlePreviewPanel() {
        super();
        this.setBackground(0x80000000);
        this.setBorder(new Border(0xff00ff00,1));
    }
}
