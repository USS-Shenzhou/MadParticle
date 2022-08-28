package cn.ussshenzhou.madparticle.command;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableIntegerArgument;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableStringReader;
import cn.ussshenzhou.madparticle.network.MadParticlePacket;
import cn.ussshenzhou.madparticle.network.MadParticlePacketSend;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Collection;

/**
 * @author USS_Shenzhou
 */
public class MadParticleCommand {
    //TODO:add particle roll

    public MadParticleCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("madparticle")
                        .redirect(dispatcher.register(Commands.literal("mp")
                                        .then(Commands.argument("targetParticle", ParticleArgument.particle())
                                                .then(Commands.argument("spriteFrom", EnumArgument.enumArgument(MadParticle.SpriteFrom.class))
                                                        .then(Commands.argument("lifeTime", InheritableIntegerArgument.inheritableInteger(40))
                                                                .then(Commands.argument("alwaysRender", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("spawnPos", Vec3Argument.vec3())
                                                                                        .then(Commands.argument("spawnDiffuse", Vec3Argument.vec3())
                                                                                                .then(Commands.argument("spawnSpeed", Vec3Argument.vec3())
                                                                                                        .then(Commands.argument("speedDiffuse", Vec3Argument.vec3())
                                                                                                                .then(Commands.argument("collision", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                                                                        .then(Commands.argument("bounceTime", InheritableIntegerArgument.inheritableInteger(40))
                                                                                                                                .then(Commands.argument("horizontalRelativeCollisionDiffuse", DoubleArgumentType.doubleArg())
                                                                                                                                        .then(Commands.argument("verticalRelativeCollisionBounce", DoubleArgumentType.doubleArg())
                                                                                                                                                .then(Commands.argument("friction", FloatArgumentType.floatArg())
                                                                                                                                                        .then(Commands.argument("afterCollisionFriction", FloatArgumentType.floatArg())
                                                                                                                                                                .then(Commands.argument("gravity", FloatArgumentType.floatArg())
                                                                                                                                                                        .then(Commands.argument("afterCollisionGravity", FloatArgumentType.floatArg())
                                                                                                                                                                                .then(Commands.argument("interactWithEntity", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                                                                                                                                        .then(Commands.argument("horizontalInteractFactor", DoubleArgumentType.doubleArg())
                                                                                                                                                                                                .then(Commands.argument("verticalInteractFactor", DoubleArgumentType.doubleArg())
                                                                                                                                                                                                        .then(Commands.argument("renderType", EnumArgument.enumArgument(ParticleRenderTypes.class))
                                                                                                                                                                                                                .then(Commands.argument("r", FloatArgumentType.floatArg())
                                                                                                                                                                                                                        .then(Commands.argument("g", FloatArgumentType.floatArg())
                                                                                                                                                                                                                                .then(Commands.argument("b", FloatArgumentType.floatArg())
                                                                                                                                                                                                                                        .then(Commands.argument("beginAlpha", FloatArgumentType.floatArg(0, 1))
                                                                                                                                                                                                                                                .then(Commands.argument("endAlpha", FloatArgumentType.floatArg(0, 1))
                                                                                                                                                                                                                                                        .then(Commands.argument("alphaMode", EnumArgument.enumArgument(MadParticle.ChangeMode.class))
                                                                                                                                                                                                                                                                .then(Commands.argument("beginScale", FloatArgumentType.floatArg(0))
                                                                                                                                                                                                                                                                        .then(Commands.argument("endScale", FloatArgumentType.floatArg(0))
                                                                                                                                                                                                                                                                                .then(Commands.argument("scaleMode", EnumArgument.enumArgument(MadParticle.ChangeMode.class))
                                                                                                                                                                                                                                                                                        .executes(ct1 -> sendToAll(ct1,dispatcher))
                                                                                                                                                                                                                                                                                        .then(Commands.argument("whoCanSee", EntityArgument.players())
                                                                                                                                                                                                                                                                                                .executes((ct) -> sendToPlayer(ct, EntityArgument.getPlayers(ct, "whoCanSee"), dispatcher))
                                                                                                                                                                                                                                                                                                .then(Commands.literal("expireThen")
                                                                                                                                                                                                                                                                                                        .redirect(dispatcher.register(Commands.literal("mp")))
                                                                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                )
                                                                                                                                                                                                                        )
                                                                                                                                                                                                                )
                                                                                                                                                                                                        )
                                                                                                                                                                                                )
                                                                                                                                                                                        )
                                                                                                                                                                                )
                                                                                                                                                                        )
                                                                                                                                                                )
                                                                                                                                                        )
                                                                                                                                                )
                                                                                                                                        )
                                                                                                                                )
                                                                                                                        )
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int sendToAll(CommandContext<CommandSourceStack> ct, CommandDispatcher<CommandSourceStack> dispatcher) {
        ServerLevel level = ct.getSource().getLevel();
        return sendToPlayer(ct, level.getPlayers(serverPlayer -> true),dispatcher);
    }

    private static int sendToPlayer(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, CommandDispatcher<CommandSourceStack> dispatcher) {
        String commandString = context.getInput();
        String[] commandStrings = commandString.split(" expireThen ");
        MadParticleOption child = null;
        for (int i = commandStrings.length - 1; i >= 0; i--) {
            String s = commandStrings[i];
            if (s.startsWith("/")){
                s = s.replaceFirst("/","");
            }
            if (!s.startsWith("mp") && !s.startsWith("madparticle")) {
                s = "mp " + s;
            }
            ParseResults<CommandSourceStack> parseResults = dispatcher.parse(s, context.getSource());
            CommandContext<CommandSourceStack> ct = parseResults.getContext().build(s);
            Vec3 pos = ct.getArgument("spawnPos", Coordinates.class).getPosition(ct.getSource());
            Vec3 posDiffuse = ct.getArgument("spawnDiffuse", WorldCoordinates.class).getPosition(ct.getSource());
            Vec3 speed = ct.getArgument("spawnSpeed", WorldCoordinates.class).getPosition(ct.getSource());
            Vec3 speedDiffuse = ct.getArgument("speedDiffuse", WorldCoordinates.class).getPosition(ct.getSource());
            boolean haveChild = i != commandStrings.length - 1;
            MadParticleOption father = new MadParticleOption(
                    Registry.PARTICLE_TYPE.getId(ct.getArgument("targetParticle", ParticleOptions.class).getType()),
                    ct.getArgument("spriteFrom", MadParticle.SpriteFrom.class),
                    ct.getArgument("lifeTime", Integer.class),
                    ct.getArgument("alwaysRender", InheritableBoolean.class),
                    ct.getArgument("amount", Integer.class),
                    pos.x, pos.y, pos.z,
                    posDiffuse.x, posDiffuse.y, posDiffuse.z,
                    speed.x, speed.y, speed.z,
                    speedDiffuse.x, speedDiffuse.y, speedDiffuse.z,
                    ct.getArgument("friction", Float.class),
                    ct.getArgument("gravity", Float.class),
                    ct.getArgument("collision", InheritableBoolean.class),
                    ct.getArgument("bounceTime", Integer.class),
                    ct.getArgument("horizontalRelativeCollisionDiffuse", Double.class),
                    ct.getArgument("verticalRelativeCollisionBounce", Double.class),
                    ct.getArgument("afterCollisionFriction", Float.class),
                    ct.getArgument("afterCollisionGravity", Float.class),
                    ct.getArgument("interactWithEntity", InheritableBoolean.class),
                    ct.getArgument("horizontalInteractFactor", Double.class),
                    ct.getArgument("verticalInteractFactor", Double.class),
                    ct.getArgument("renderType", ParticleRenderTypes.class),
                    ct.getArgument("r", Float.class), ct.getArgument("g", Float.class), ct.getArgument("b", Float.class),
                    ct.getArgument("beginAlpha", Float.class),
                    ct.getArgument("endAlpha", Float.class),
                    ct.getArgument("alphaMode", MadParticle.ChangeMode.class),
                    ct.getArgument("beginScale", Float.class),
                    ct.getArgument("endScale", Float.class),
                    ct.getArgument("scaleMode", MadParticle.ChangeMode.class),
                    haveChild,
                    haveChild ? child : null
            );
            child = father;
        }
        for (ServerPlayer player : players) {
            MadParticlePacketSend.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new MadParticlePacket(child)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

}
