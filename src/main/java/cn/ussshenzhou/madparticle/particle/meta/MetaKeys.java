package cn.ussshenzhou.madparticle.particle.meta;

/**
 * @author USS_Shenzhou
 */

public enum MetaKeys {
    TADA("tada"),
    DX("dx"),
    DY("dy"),
    DZ("dz"),
    LIFE_ERROR("life")
    ;
    private final String key;

    private MetaKeys(String key){
        this.key = key;
    }

    public String get() {
        return key;
    }
}
