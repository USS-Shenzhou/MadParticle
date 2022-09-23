package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TTitledSelectList<E> extends TTitledComponent<TSelectList<E>> {
    int gap = 0;

    public TTitledSelectList(Component titleText, TSelectList<E> component) {
        super(titleText, component);
        this.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.setLabelHeight(20);
    }

    @Override
    public void defaultLayout() {
        title.setBounds(0, 0, this.width - widget.getScrollbarGap() - TSelectList.SCROLLBAR_WIDTH, labelHeight);
        LayoutHelper.BBottomOfA(widget, gap, title, width, height - title.getHeight() - gap);
    }

    public void addElement(E element) {
        widget.addElement(element);
    }

    public void addElement(E element, Consumer<TSelectList<E>> onSelected) {
        widget.addElement(element, onSelected);
    }

    public void addElement(Collection<E> elements) {
        widget.addElement(elements);
    }
}
