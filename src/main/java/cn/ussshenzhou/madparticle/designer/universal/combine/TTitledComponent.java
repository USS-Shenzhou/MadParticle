package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TLabel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public abstract class TTitledComponent<T extends TWidget> extends TPanel {
    protected final TLabel title = new TLabel();
    protected final T widget;
    int gap = 0;
    int labelHeight = 16;

    public TTitledComponent(Component titleText, T component) {
        widget = component;
        title.setText(titleText);
        title.setHorizontalAlignment(HorizontalAlignment.LEFT);
        this.add(title);
        this.add(widget);
    }

    @Override
    public void layout() {
        defaultLayout();
        super.layout();
    }

    public void defaultLayout() {
        title.setBounds(0, 0, width, labelHeight);
        LayoutHelper.BBottomOfA(widget, gap, title, width, height - title.getHeight() - gap);
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public TLabel getTitle() {
        return title;
    }

    public T getComponent() {
        return widget;
    }

    public int getLabelHeight() {
        return labelHeight;
    }

    public void setLabelHeight(int labelHeight) {
        this.labelHeight = labelHeight;
    }
}
