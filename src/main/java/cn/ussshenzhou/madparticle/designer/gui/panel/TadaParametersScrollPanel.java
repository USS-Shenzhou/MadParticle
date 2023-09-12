package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.widegt.SingleVec3EditBox;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class TadaParametersScrollPanel extends ParametersScrollPanel {
    public final SingleVec3EditBox speed = new SingleVec3EditBox(Component.literal("V"));

    public TadaParametersScrollPanel() {
        super();
        //I do not know why, just dont delete scaleBegin.
        this.addAll(speed, scaleBegin);
        List.of(vx, vy, vz).forEach(singleVec3EditBox -> {
            singleVec3EditBox.setVisibleT(false);
            singleVec3EditBox.getComponent().setEditable(false);
        });
        speed.getComponent().addPassedResponder(s -> {
            List.of(vx, vy, vz).forEach(singleVec3EditBox -> singleVec3EditBox.getComponent().setValue(s));
        });
        var tadaMetaPair = this.metaPanel.createAPair();
        tadaMetaPair.setKV("tada", "1");
        tadaMetaPair.key.getEditBox().setEditable(false);
        tadaMetaPair.value.setEditable(false);
        tadaMetaPair.remove.setVisibleT(false);
        addDemoLimitToolTip();
    }

    private void addDemoLimitToolTip() {
        Map.ofEntries(
                Map.entry(speed,"0.0 ~ 0.5"),
                Map.entry(lifeTime, "0 ~ 100"),
                Map.entry(amount, "0 ~ 10"),
                Map.entry(vx, "-1.0 ~ 1.0"),
                Map.entry(vy, "-1.0 ~ 1.0"),
                Map.entry(vz, "-1.0 ~ 1.0"),
                Map.entry(vxD, "-1 ~ 1.0"),
                Map.entry(vyD, "-1.0 ~ 1.0"),
                Map.entry(vzD, "-1.0 ~ 1.0"),
                Map.entry(r, "0 ~ 10"),
                Map.entry(g, "0 ~ 10"),
                Map.entry(b, "0 ~ 10"),
                Map.entry(horizontalCollision, "-2 ~ 2"),
                Map.entry(verticalCollision, "-2 ~ 2"),
                Map.entry(collisionTime, "0 ~ 3"),
                Map.entry(xDeflection, "-0.5 ~ 0.5"),
                Map.entry(xDeflection2, "-0.5 ~ 0.5"),
                Map.entry(zDeflection, "-0.5 ~ 0.5"),
                Map.entry(zDeflection2, "-0.5 ~ 0.5"),
                Map.entry(roll, "-0.5 ~ 0.5"),
                Map.entry(horizontalInteract, "-2 ~ 2"),
                Map.entry(verticalInteract, "-2 ~ 2"),
                Map.entry(friction, "0 ~ 1"),
                Map.entry(friction2, "0 ~ 1"),
                Map.entry(gravity, "-0.5 ~ 0.5"),
                Map.entry(gravity2, "-0.5 ~ 0.5"),
                Map.entry(bloomStrength, "0 / 1"),
                Map.entry(alphaBegin, "0 ~ 1"),
                Map.entry(alphaEnd, "0 ~ 1"),
                Map.entry(scaleBegin, "0 ~ 5"),
                Map.entry(scaleEnd, "0 ~ 5")
        ).forEach((w, t) -> w.getComponent().setTooltip(Tooltip.create(Component.literal(t))));
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
    public void setChild(boolean child) {
        super.setChild(child);
        List.of(xPos, yPos, zPos, vx, vy, vz).forEach(singleVec3EditBox -> {
            singleVec3EditBox.getComponent().setValue(isChild ? "=" : "~");
            AccessorProxy.EditBoxProxy.setDisplayPos(singleVec3EditBox.getComponent(), 0);
            singleVec3EditBox.getComponent().setEditable(false);
        });
        if (isChild) {
            speed.setVisibleT(false);
            List.of(vx, vy, vz).forEach(box -> box.setVisibleT(true));
        }
    }
}
