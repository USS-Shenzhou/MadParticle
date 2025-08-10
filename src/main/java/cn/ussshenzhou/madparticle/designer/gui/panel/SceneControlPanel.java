package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.api.AddParticleHelperC;
import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.t88.gui.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.t88.gui.container.TVerticalScrollContainer;
import cn.ussshenzhou.t88.gui.notification.TSimpleNotification;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.ClientCommandSourceStack;

/**
 * @author USS_Shenzhou
 */
public class SceneControlPanel extends TVerticalScrollContainer {

    private static final CommandDispatcher<CommandSourceStack> DISPATCHER = new CommandDispatcher<>();

    static {
        new MadParticleCommand(DISPATCHER);
    }

    private final TTitledSimpleConstrainedEditBox period = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.period"), IntegerArgumentType.integer(1));
    private final TTitledSimpleConstrainedEditBox x = new TTitledSimpleConstrainedEditBox(Component.literal("x"), FloatArgumentType.floatArg());
    private final TTitledSimpleConstrainedEditBox y = new TTitledSimpleConstrainedEditBox(Component.literal("y"), FloatArgumentType.floatArg());
    private final TTitledSimpleConstrainedEditBox z = new TTitledSimpleConstrainedEditBox(Component.literal("z"), FloatArgumentType.floatArg());
    private int age = 0;

    public SceneControlPanel() {
        period.getComponent().setValue(String.valueOf(1));
        this.add(period);
        x.setTooltip(Tooltip.create(Component.translatable("gui.mp.de.helper.abs")));
        this.add(x);
        y.setTooltip(Tooltip.create(Component.translatable("gui.mp.de.helper.abs")));
        this.add(y);
        z.setTooltip(Tooltip.create(Component.translatable("gui.mp.de.helper.abs")));
        this.add(z);
    }

    @Override
    public void layout() {
        period.setBounds(DesignerScreen.GAP, DesignerScreen.GAP, getUsableWidth() - 2 * DesignerScreen.GAP, 20 + 12);
        LayoutHelper.BBottomOfA(x, DesignerScreen.GAP, period);
        LayoutHelper.BBottomOfA(y, DesignerScreen.GAP, x);
        LayoutHelper.BBottomOfA(z, DesignerScreen.GAP, y);
        super.layout();
    }

    @Override
    public void tickT() {
        sendParticle();
        age++;
        super.tickT();
    }

    private void sendParticle() {
        try {
            var p = Integer.parseInt(period.getComponent().getValue());
            if (p > 0 && age % p == 0) {
                if (Minecraft.getInstance().getFps() <= 20) {
                    TSimpleNotification.fire(Component.translatable("gui.mp.de.helper.preview_fail_fps"), 6, TSimpleNotification.Severity.WARN);
                    return;
                }
                var xs = x.getComponent().getValue();
                var ys = y.getComponent().getValue();
                var zs = z.getComponent().getValue();
                var source = ClientCommandHandler.getSource();
                if (!xs.isEmpty() && !ys.isEmpty() && !zs.isEmpty()) {
                    source.withPosition(new Vec3(Double.parseDouble(xs), Double.parseDouble(ys), Double.parseDouble(zs)));
                }
                var particle = MadParticleCommand.assembleOption(this.getParentInstanceOf(HelperModePanel.class).command.getEditBox().getValue(), source, DISPATCHER);
                if (particle != null) {
                    AddParticleHelperC.addParticleClient(particle);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
