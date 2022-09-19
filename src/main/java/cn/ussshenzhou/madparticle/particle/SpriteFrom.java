package cn.ussshenzhou.madparticle.particle;

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum SpriteFrom {
    RANDOM("gui.mp.de.helper.sprite.random"),
    AGE("gui.mp.de.helper.sprite.age"),
    INHERIT("gui.mp.de.helper.inherit");

    private final String translateKey;

    private SpriteFrom(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String toString() {
        return translateKey;
    }
}
