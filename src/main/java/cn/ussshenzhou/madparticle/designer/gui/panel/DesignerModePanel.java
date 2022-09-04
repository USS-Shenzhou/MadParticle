package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.widegt.DesignerModeSelector;
import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TLabel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author USS_Shenzhou
 */
public class DesignerModePanel extends TPanel {
    private final TLabel title = new TLabel(new TranslatableComponent("gui.mp.de.mode.title"));
    private final DesignerModeSelector modeSelector = new DesignerModeSelector();

    public DesignerModePanel() {
        super();
        title.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.add(title);
        this.add(modeSelector);
    }

    @Override
    public void layout() {
        title.setBounds(0, 0, this.width - modeSelector.getScrollbarGap() - TSelectList.SCROLLBAR_WIDTH, 20);
        LayoutHelper.BBottomOfA(title, 0, modeSelector, width, height - title.getHeight());
        super.layout();
    }

    public DesignerModeSelector getModeSelector() {
        return modeSelector;
    }
}
