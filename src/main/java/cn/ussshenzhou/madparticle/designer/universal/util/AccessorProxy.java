package cn.ussshenzhou.madparticle.designer.universal.util;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TSlider;
import cn.ussshenzhou.madparticle.mixin.AbstractSelectionListAccessor;
import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import cn.ussshenzhou.madparticle.mixin.SliderButtonAccessor;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * @author Tony Yu
 */
public class AccessorProxy {
    public static class EditBoxProxy {
        public static int getDisplayPos(EditBox that) {
            return ((EditBoxAccessor) that).getDisplayPos();
        }

        public static void setDisplayPos(EditBox that, int pos) {
            ((EditBoxAccessor) that).setDisplayPos(pos);
        }

        public static boolean isEditBoxEdible(EditBox that) {
            return ((EditBoxAccessor) that).isIsEditable();
        }
    }


    @SuppressWarnings("unchecked")
    public static class AbstractSelectionListProxy {
        public static <E extends AbstractSelectionList.Entry<E>> void setHovered(AbstractSelectionList<E> that, E hovered) {
            ((AbstractSelectionListAccessor<E>) that).setHovered(hovered);
        }

        public static <E extends AbstractSelectionList.Entry<E>> boolean isRenderHeader(AbstractSelectionList<E> that) {
            return ((AbstractSelectionListAccessor<E>) that).isRenderHeader();
        }


        public static <E extends AbstractSelectionList.Entry<E>> boolean isRenderBackground(AbstractSelectionList<E> that) {
            return ((AbstractSelectionListAccessor<E>) that).isRenderBackground();
        }


        public static <E extends AbstractSelectionList.Entry<E>> boolean isRenderTopAndBottom(AbstractSelectionList<E> that) {
            return ((AbstractSelectionListAccessor<E>) that).isRenderTopAndBottom();
        }
    }

    public static class SliderProxy {
        public static void setOption(TSlider that, ProgressOption option) {
            ((SliderButtonAccessor) that).setOption(option);
        }

        public static void setToolTip(TSlider that, List<FormattedCharSequence> tooltip) {
            ((SliderButtonAccessor) that).setTooltip(tooltip);
        }
    }
}
