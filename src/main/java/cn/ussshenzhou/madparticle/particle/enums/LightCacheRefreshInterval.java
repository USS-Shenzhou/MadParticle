package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */
public enum LightCacheRefreshInterval implements ITranslatable {
    TICK20("gui.mp.de.setting.light.update.tick20", 20),
    TICK10("gui.mp.de.setting.light.update.tick10", 10),
    TICK5("gui.mp.de.setting.light.update.tick5", 5),
    TICK1("gui.mp.de.setting.light.update.tick1", 1),
    FRAME("gui.mp.de.setting.light.update.frame", 0);

    private final String translateKey;
    private final int interval;

    LightCacheRefreshInterval(String translateKey, int interval) {
        this.translateKey = translateKey;
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }
}
