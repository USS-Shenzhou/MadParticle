package cn.ussshenzhou.madparticle.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author USS_Shenzhou
 */
public class MadParticleCommand {

    public MadParticleCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("madparticle")
                        .redirect(dispatcher.register(Commands.literal("mp")
                        .then(Commands.argument())
                ))
        );
    }
}
