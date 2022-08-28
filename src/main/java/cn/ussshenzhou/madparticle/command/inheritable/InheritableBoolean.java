package cn.ussshenzhou.madparticle.command.inheritable;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum InheritableBoolean {
    TRUE(true),
    FALSE(false),
    INHERIT(false);

    private final boolean value;

    private InheritableBoolean(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public static InheritableBoolean wrap(boolean value) {
        return value ? TRUE : FALSE;
    }
}
