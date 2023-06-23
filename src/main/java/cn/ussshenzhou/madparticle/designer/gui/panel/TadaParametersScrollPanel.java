package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.widegt.SingleVec3EditBox;
import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class TadaParametersScrollPanel extends ParametersScrollPanel {
    public final SingleVec3EditBox speed = new SingleVec3EditBox(Component.literal("V"));

    public TadaParametersScrollPanel() {

        //I do not know why, just dont delete scaleBegin.
        this.addAll(speed, scaleBegin);

        List.of(xPos, yPos, zPos).forEach(singleVec3EditBox -> {
            singleVec3EditBox.getComponent().setValue("~");
            AccessorProxy.EditBoxProxy.setDisplayPos(singleVec3EditBox.getComponent(), 0);
            singleVec3EditBox.getComponent().setEditable(false);
        });
        List.of(vx, vy, vz).forEach(singleVec3EditBox -> {
            singleVec3EditBox.setVisibleT(false);
            singleVec3EditBox.getComponent().setEditable(false);
        });
        speed.getComponent().addPassedResponder(s -> {
            List.of(vx, vy, vz).forEach(singleVec3EditBox -> singleVec3EditBox.getComponent().setValue(s));
        });
    }

    @Override
    public void layout() {
        super.layout();
        int xGap = vy.getXT() - vx.getXT() - vx.getWidth();
        LayoutHelper.BLeftOfA(speed, xGap, vxD, vx.getWidth() * 3 + 2 * xGap, vx.getHeight());
    }

    @Override
    protected void tryFillDefault() {
        super.tryFillDefault();
        ifClearThenSet(speed, isChild ? "=" : "0.0");
    }

    @Override
    public String wrap() {
        return super.wrap() + " {\"tada\":1}";
    }
}
