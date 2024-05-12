package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */
public enum TranslucentMethod implements ITranslatable {
    DEPTH_TRUE("gui.mp.de.setting.universal.translucent.true"),
    DEPTH_FALSE("gui.mp.de.setting.universal.translucent.false"),
    //TODO
    OIT("gui.mp.de.setting.universal.translucent.oit");

    private final String translationKey;

    TranslucentMethod(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String translateKey() {
        return translationKey;
    }
}
