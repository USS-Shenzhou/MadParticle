package cn.ussshenzhou.madparticle.command.inheritable;


import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum InheritableBoolean implements ITranslatable {
    TRUE(true, "gui.mp.de.helper.true"),
    FALSE(false, "gui.mp.de.helper.false"),
    INHERIT(false, "gui.mp.de.helper.inherit");

    private final boolean value;
    private final String translateKey;

    private InheritableBoolean(boolean value, String translateKey) {
        this.value = value;
        this.translateKey = translateKey;
    }

    public boolean get() {
        return value;
    }

    public static InheritableBoolean wrap(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }
}
