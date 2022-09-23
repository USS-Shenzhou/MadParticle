package cn.ussshenzhou.madparticle.designer.universal.event;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TEditBox;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author USS_Shenzhou
 */
public class EditBoxFocusedEvent extends Event {
    private final TEditBox willFocused;

    public EditBoxFocusedEvent(TEditBox willFocused) {
        this.willFocused = willFocused;
    }

    public TEditBox getWillFocused() {
        return willFocused;
    }
}
