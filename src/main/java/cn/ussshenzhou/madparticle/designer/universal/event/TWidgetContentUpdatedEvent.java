package cn.ussshenzhou.madparticle.designer.universal.event;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TWidget;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Tony Yu
 */
public class TWidgetContentUpdatedEvent extends Event {
    private final TWidget updated;

    public TWidgetContentUpdatedEvent(TWidget updated) {
        this.updated = updated;
    }

    public TWidget getUpdated() {
        return updated;
    }
}
