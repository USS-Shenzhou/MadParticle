package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;

/**
 * @author USS_Shenzhou
 */
public class DesignerModeSelector extends TSelectList<DesignerModeSelector.DesignerMode> {
    public DesignerModeSelector() {
        super();
        this.addElement(DesignerMode.HELPER, list -> {
            //TODO
        });
        this.addElement(DesignerMode.LINE, list -> {

        });
    }

    @SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
    public enum DesignerMode {
        HELPER("gui.mp.de.mode.helper"),
        LINE("gui.mp.de.mode.line");

        private final String translateKey;

        private DesignerMode(String translateKey) {
            this.translateKey = translateKey;
        }

        @Override
        public String toString() {
            return this.translateKey;
        }
    }
}
