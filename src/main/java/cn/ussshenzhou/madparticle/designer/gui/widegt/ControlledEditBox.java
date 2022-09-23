package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;

import java.util.function.Predicate;

public class ControlledEditBox<C> extends TEditBox {
    Predicate<C> predicate;

    public ControlledEditBox(Predicate<C> predicate) {
        super();
        this.predicate = predicate;
    }

    public void test(C condition) {
        if (this.predicate.test(condition)) {
            this.accept();
        } else {
            this.deny();
        }
    }

    public void accept(){

    }

    public void deny(){

    }
}
