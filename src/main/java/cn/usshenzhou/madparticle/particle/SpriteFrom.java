package cn.usshenzhou.madparticle.particle;

import cn.usshenzhou.madparticle.command.ToTranslatableString;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum SpriteFrom implements ToTranslatableString {
    RANDOM("gui.mp.de.helper.sprite.random"),
    AGE("gui.mp.de.helper.sprite.age"),
    INHERIT("gui.mp.de.helper.inherit");

    private final String translateKey;

    private SpriteFrom(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String toTranslateKey() {
        return translateKey;
    }
}
