package cn.ussshenzhou.madparticle.designer.universal.advanced;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public abstract class TConstrainedEditBox extends TEditBox {
    protected final LinkedList<Consumer<String>> passedResponders = new LinkedList<>();
    public static final int RED_TEXT_COLOR = 0xfc5454;
    public static final int WHITE_TEXT_COLOR = DEFAULT_TEXT_COLOR;
    public static final int BLUE_TEXT_COLOR = 0x37e2ff;

    public TConstrainedEditBox() {
        super();
        this.addResponder(s -> {
            if (check(s)) {
                passedRespond(s);
                this.setTextColor(WHITE_TEXT_COLOR);
            }
        });
    }

    public abstract void checkAndThrow(String value) throws CommandSyntaxException;

    public boolean check(String value) {
        try {
            checkAndThrow(value);
        } catch (CommandSyntaxException e) {
            this.setTextColor(RED_TEXT_COLOR);
            return false;
        } catch (StringIndexOutOfBoundsException e) {
            return true;
        } catch (Exception ignored) {
            return false;
            //this.setTextColor(0x37e2ff);
            //return;
        }
        //this.setTextColor(14737632);
        return true;
    }

    public void passedRespond(String value) {
        passedResponders.forEach(stringConsumer -> stringConsumer.accept(value));
    }


    public void addPassedResponder(Consumer<String> responder) {
        passedResponders.add(responder);
    }


    public void clearPassedResponders() {
        passedResponders.clear();
    }
}
