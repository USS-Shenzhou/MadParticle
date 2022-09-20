package cn.ussshenzhou.madparticle.designer.universal.advanced;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public abstract class TConstrainedEditBox extends TEditBox {

    public TConstrainedEditBox() {
        super();
        this.setResponder(this::check);
    }

    public abstract void checkAndThrow(String value) throws CommandSyntaxException;

    public void check(String value) {
        try {
            checkAndThrow(value);
        } catch (CommandSyntaxException e) {
            this.setTextColor(0xfc5454);
            return;
        } catch (Exception ignored) {
            //this.setTextColor(0x37e2ff);
            //return;
        }
        this.setTextColor(14737632);
    }
}
