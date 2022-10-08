package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"), cancellable = true)
    private void madParticleOptimize(CommandSourceStack pSource, String pCommand, CallbackInfoReturnable<Integer> cir) {
        if (pCommand.startsWith("mp ")
                //execute ... mp ...
                || pCommand.contains(" mp ")) {
            CompletableFuture.runAsync(() -> MadParticleCommand.fastSend(pCommand, pSource, pSource.getLevel().getPlayers(serverPlayer -> true), dispatcher));
            cir.setReturnValue(Command.SINGLE_SUCCESS);
        }
    }
}
