package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import net.minecraft.client.gui.components.EditBox;

public class EditBoxAccessorProxy {
    public static int getDisplayPos(EditBox editBox) {
        return ((EditBoxAccessor) editBox).getDisplayPos();
    }

    public static void setDisplayPos(EditBox editBox, int pos) {
        ((EditBoxAccessor) editBox).setDisplayPos(pos);
    }

    public static boolean isEdible(EditBox editBox) {
        return ((EditBoxAccessor) editBox).isIsEditable();
    }
}
