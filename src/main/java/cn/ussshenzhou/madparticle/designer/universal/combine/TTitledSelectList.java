package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TLabel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TTitledSelectList<E> extends TPanel {
    private final TLabel title = new TLabel();
    private final TSelectList<E> list = new TSelectList<>();
    int gap = 0;

    public TTitledSelectList(Component titleText) {
        title.setText(titleText);
        title.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.add(title);
        this.add(list);
    }

    @Override
    public void layout() {
        title.setBounds(0, 0, this.width - list.getScrollbarGap() - TSelectList.SCROLLBAR_WIDTH, 20);
        LayoutHelper.BBottomOfA(title, gap, list, width, height - title.getHeight() - gap);
        super.layout();
    }

    public void addElement(E element) {
        list.addElement(element);
    }

    public void addElement(E element, Consumer<TSelectList<E>> onSelected) {
        list.addElement(element, onSelected);
    }

    public void addElement(Collection<E> elements) {
        list.addElement(elements);
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

    public TSelectList<E> getList() {
        return list;
    }
}
