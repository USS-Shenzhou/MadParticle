package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.designer.gui.widegt.SingleVec3EditBox;
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
        speed.setAbsBounds(vx.getXT(), vx.getYT(), vx.getWidth() * 3 + 2 * xGap, vx.getHeight());
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
