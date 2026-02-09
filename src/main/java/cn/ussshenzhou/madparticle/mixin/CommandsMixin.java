package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
@Mixin(Commands.class)
public class CommandsMixin {

    @Final
    @Shadow
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "performPrefixedCommand", at = @At("HEAD"), cancellable = true)
    private void madParticleOptimize(CommandSourceStack pSource, String pCommand, CallbackInfo ci) {
        if (pCommand.startsWith("mp ")
                //execute ... mp ...
                || pCommand.contains(" mp ")) {
            if (pSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)){
                Thread.startVirtualThread(() -> MadParticleCommand.fastSend(pCommand, pSource, dispatcher));
            }
            ci.cancel();
        }
    }
}
