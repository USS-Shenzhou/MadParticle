package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.widegt.DesignerModeSelectList;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(AbstractCommandBlockEditScreen.class)
public class AbstractCommandBlockEditScreenMixin extends Screen {

    @Shadow
    protected EditBox commandEdit;

    protected AbstractCommandBlockEditScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 150, ordinal = 4))
    private int madparticleLongerCommandEdit1(int constant) {
        return madparticleIsOptimizeEnabled() ? (int) (this.width * 0.9f / 2) : 150;
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 300))
    private int madparticleLongerCommandEdit2(int constant) {
        return madparticleIsOptimizeEnabled() ? (int) (this.width * 0.9f) : 300;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void madparticleToDesignerButton(CallbackInfo ci) {
        if (madparticleIsOptimizeEnabled()){
            var button = new TButton(Component.translatable("gui.mp.optimize_command_block.to_designer"), b -> {
                var s = DesignerScreen.newInstance();
                Minecraft.getInstance().setScreen(s);
                s.initFromCommand(this.commandEdit.getValue());
                s.setVisibleMode(DesignerModeSelectList.DesignerMode.HELPER);
            });
            button.setAbsBounds((int) (this.width - this.width * 0.05 - 150), 50 + 20 + 4, 150, 20);
            this.addRenderableWidget(button);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void madparticleToDesignerButtonTick(CallbackInfo ci) {
        if (madparticleIsOptimizeEnabled()){
            this.children().stream()
                    .filter(g -> g instanceof TButton)
                    .findFirst()
                    .ifPresent(b -> {
                        var button = (TButton) b;
                        var command = this.commandEdit.getValue();
                        var visible = command.isEmpty()
                                || command.startsWith("mp")
                                || command.startsWith("madparticle")
                                || command.startsWith("/mp")
                                || command.startsWith("/madparticle");
                        button.setVisibleT(visible);
                    });
        }
    }

    @Unique
    private boolean madparticleIsOptimizeEnabled() {
        return ConfigHelper.getConfigRead(MadParticleConfig.class).optimizeCommandBlockEditScreen;
    }
}
