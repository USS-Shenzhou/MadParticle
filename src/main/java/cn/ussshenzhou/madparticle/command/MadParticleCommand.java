package cn.ussshenzhou.madparticle.command;

import cn.ussshenzhou.madparticle.command.inheritable.*;
import cn.ussshenzhou.madparticle.network.MadParticlePacket;
import cn.ussshenzhou.madparticle.network.MadParticleTadaPacket;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.madparticle.particle.enums.MetaKeys;
import cn.ussshenzhou.t88.T88;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.server.command.EnumArgument;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
public class MadParticleCommand {
    private static final int COMMAND_LENGTH = 40;

    public MadParticleCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("madparticle")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .redirect(dispatcher.register(Commands.literal("mp")
                                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                                        .then(Commands.argument("targetParticle", ParticleArgument.particle(Commands.createValidationContext(VanillaRegistries.createLookup())))
                                                .then(Commands.argument("spriteFrom", EnumArgument.enumArgument(SpriteFrom.class))
                                                        .then(Commands.argument("lifeTime", new InheritableIntegerArgument(0, Integer.MAX_VALUE, COMMAND_LENGTH))
                                                                .then(Commands.argument("alwaysRender", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("spawnPos", InheritableVec3Argument.inheritableVec3(COMMAND_LENGTH))
                                                                                        .then(Commands.argument("spawnDiffuse", Vec3Argument.vec3(false))
                                                                                                .then(Commands.argument("spawnSpeed", InheritableVec3Argument.inheritableVec3(COMMAND_LENGTH))
                                                                                                        .then(Commands.argument("speedDiffuse", Vec3Argument.vec3(false))
                                                                                                                .then(Commands.argument("collision", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                                                                        .then(Commands.argument("bounceTime", InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE, COMMAND_LENGTH))
                                                                                                                                .then(Commands.argument("horizontalRelativeCollisionDiffuse", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                        .then(Commands.argument("verticalRelativeCollisionBounce", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                .then(Commands.argument("friction", FloatArgumentType.floatArg())
                                                                                                                                                        .then(Commands.argument("afterCollisionFriction", FloatArgumentType.floatArg())
                                                                                                                                                                .then(Commands.argument("gravity", FloatArgumentType.floatArg())
                                                                                                                                                                        .then(Commands.argument("afterCollisionGravity", FloatArgumentType.floatArg())
                                                                                                                                                                                .then(Commands.argument("xDeflection", FloatArgumentType.floatArg())
                                                                                                                                                                                        .then(Commands.argument("xDeflectionAfterCollision", FloatArgumentType.floatArg())
                                                                                                                                                                                                .then(Commands.argument("zDeflection", FloatArgumentType.floatArg())
                                                                                                                                                                                                        .then(Commands.argument("zDeflectionAfterCollision", FloatArgumentType.floatArg())
                                                                                                                                                                                                                .then(Commands.argument("rollSpeed", InheritableFloatArgument.inheritableFloat())
                                                                                                                                                                                                                        .then(Commands.argument("interactWithEntity", EnumArgument.enumArgument(InheritableBoolean.class))
                                                                                                                                                                                                                                .then(Commands.argument("horizontalInteractFactor", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                                                                                                        .then(Commands.argument("verticalInteractFactor", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                                                                                                                .then(Commands.argument("renderType", EnumArgument.enumArgument(ParticleRenderTypes.class))
                                                                                                                                                                                                                                                        .then(Commands.argument("r", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                                                                                                                                .then(Commands.argument("g", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                                                                                                                                        .then(Commands.argument("b", InheritableFloatArgument.inheritableFloat(COMMAND_LENGTH))
                                                                                                                                                                                                                                                                                .then(Commands.argument("bloomFactor", new InheritableFloatArgument(1, 255, COMMAND_LENGTH))
                                                                                                                                                                                                                                                                                        .then(Commands.argument("beginAlpha", FloatArgumentType.floatArg(0, 1))
                                                                                                                                                                                                                                                                                                .then(Commands.argument("endAlpha", FloatArgumentType.floatArg(0, 1))
                                                                                                                                                                                                                                                                                                        .then(Commands.argument("alphaMode", EnumArgument.enumArgument(ChangeMode.class))
                                                                                                                                                                                                                                                                                                                .then(Commands.argument("beginScale", FloatArgumentType.floatArg(0))
                                                                                                                                                                                                                                                                                                                        .then(Commands.argument("endScale", FloatArgumentType.floatArg(0))
                                                                                                                                                                                                                                                                                                                                .then(Commands.argument("scaleMode", EnumArgument.enumArgument(ChangeMode.class))
                                                                                                                                                                                                                                                                                                                                        .executes(ct1 -> sendToAll(ct1, dispatcher))
                                                                                                                                                                                                                                                                                                                                        .then(Commands.argument("whoCanSee", EntityArgument.players())
                                                                                                                                                                                                                                                                                                                                                .executes((ct) -> sendToAssigned(ct.getInput(), ct.getSource(), EntityArgument.getPlayers(ct, "whoCanSee"), dispatcher))
                                                                                                                                                                                                                                                                                                                                                .then(Commands.argument("meta", CompoundTagArgument.compoundTag())
                                                                                                                                                                                                                                                                                                                                                        .executes((ct) -> sendToAssigned(ct.getInput(), ct.getSource(), EntityArgument.getPlayers(ct, "whoCanSee"), dispatcher))
                                                                                                                                                                                                                                                                                                                                                        .then(Commands.literal("expireThen")
                                                                                                                                                                                                                                                                                                                                                                .redirect(dispatcher.register(Commands.literal("mp")))
                                                                                                                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                                                                                                                )
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
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int sendToAll(CommandContext<CommandSourceStack> ct, CommandDispatcher<CommandSourceStack> dispatcher) {
        return sendToAssigned(ct.getInput(), ct.getSource(), ct.getSource().getLevel().getPlayers(serverPlayer -> true), dispatcher);
    }

    private static int sendToAssigned(String command, CommandSourceStack source, Collection<ServerPlayer> players, CommandDispatcher<CommandSourceStack> dispatcher) {
        var option = assembleOption(command, source, dispatcher);
        send(option, source, players);
        return Command.SINGLE_SUCCESS;
    }

    private static boolean sendTada = false;

    private static void initializeFlags() {
        sendTada = false;
    }

    public static MadParticleOption assembleOption(String commandString, CommandSourceStack sourceStack, CommandDispatcher<CommandSourceStack> dispatcher) {
        initializeFlags();
        String[] commandStrings = commandString.split(" expireThen ");
        MadParticleOption child = null;
        for (int i = commandStrings.length - 1; i >= 0; i--) {
            child = wrap(child, commandStrings, i, sourceStack, dispatcher);
        }
        return child;
    }

    private static void send(MadParticleOption option, CommandSourceStack sourceStack, Collection<ServerPlayer> targetPlayers) {
        var sourcePlayer = sourceStack.getPlayer();
        if (sendTada && sourcePlayer == null) {
            return;
        }
        Object packet = sendTada ? new MadParticleTadaPacket(option, sourcePlayer.getUUID()) : new MadParticlePacket(option);
        targetPlayers.forEach(player -> NetworkHelper.sendToPlayer(player, packet));
    }

    private static MadParticleOption wrap(@Nullable MadParticleOption child, String[] commandStrings, int index, CommandSourceStack sourceStack, CommandDispatcher<CommandSourceStack> dispatcher) {
        String s = commandStrings[index];
        if (s.startsWith("/")) {
            s = s.replaceFirst("/", "");
        }

        if (!s.startsWith("mp") && !s.startsWith("madparticle")) {
            if (!s.startsWith("execute")) {
                s = "mp " + s;
                s = s.replaceFirst("madparticle", "mp");
            }
        }
        InheritableCommandDispatcher<CommandSourceStack> inheritableCommandDispatcher = new InheritableCommandDispatcher<>(dispatcher.getRoot());
        ParseResults<CommandSourceStack> parseResults = inheritableCommandDispatcher.parse(new InheritableStringReader(s), sourceStack);
        CommandContext<CommandSourceStack> ctRoot = parseResults.getContext().build(s);
        CommandContext<CommandSourceStack> ct = CommandHelper.getContextHasArgument(ctRoot, "targetParticle", ParticleOptions.class);

        CompoundTag metaTag = null;
        try {
            metaTag = ct.getArgument("meta", CompoundTag.class);
            sendTada = metaTag.getBooleanOr(MetaKeys.TADA.get(), false);
        } catch (IllegalArgumentException ignored) {
            metaTag = new CompoundTag();
        }

        Vec3 pos = ct.getArgument("spawnPos", Coordinates.class).getPosition(sourceStack);
        Vec3 posDiffuse = ct.getArgument("spawnDiffuse", WorldCoordinates.class).getPosition(sourceStack);
        Vec3 speed = ct.getArgument("spawnSpeed", WorldCoordinates.class).getPosition(sourceStack);
        Vec3 speedDiffuse = ct.getArgument("speedDiffuse", WorldCoordinates.class).getPosition(sourceStack);
        boolean haveChild = index != commandStrings.length - 1;
        MadParticleOption father = new MadParticleOption(
                BuiltInRegistries.PARTICLE_TYPE.getId(ct.getArgument("targetParticle", ParticleOptions.class).getType()),
                ct.getArgument("spriteFrom", SpriteFrom.class),
                ct.getArgument("lifeTime", Integer.class),
                ct.getArgument("alwaysRender", InheritableBoolean.class),
                ct.getArgument("amount", Integer.class),
                pos.x, pos.y, pos.z,
                (float) posDiffuse.x, (float) posDiffuse.y, (float) posDiffuse.z,
                speed.x, speed.y, speed.z,
                (float) speedDiffuse.x, (float) speedDiffuse.y, (float) speedDiffuse.z,
                ct.getArgument("friction", Float.class),
                ct.getArgument("gravity", Float.class),
                ct.getArgument("collision", InheritableBoolean.class),
                ct.getArgument("bounceTime", Integer.class),
                ct.getArgument("horizontalRelativeCollisionDiffuse", Float.class),
                ct.getArgument("verticalRelativeCollisionBounce", Float.class),
                ct.getArgument("afterCollisionFriction", Float.class),
                ct.getArgument("afterCollisionGravity", Float.class),
                ct.getArgument("interactWithEntity", InheritableBoolean.class),
                ct.getArgument("horizontalInteractFactor", Float.class),
                ct.getArgument("verticalInteractFactor", Float.class),
                ct.getArgument("renderType", ParticleRenderTypes.class),
                ct.getArgument("r", Float.class), ct.getArgument("g", Float.class), ct.getArgument("b", Float.class),
                ct.getArgument("beginAlpha", Float.class),
                ct.getArgument("endAlpha", Float.class),
                ct.getArgument("alphaMode", ChangeMode.class),
                ct.getArgument("beginScale", Float.class),
                ct.getArgument("endScale", Float.class),
                ct.getArgument("scaleMode", ChangeMode.class),
                haveChild,
                haveChild ? child : null,
                ct.getArgument("rollSpeed", Float.class),
                ct.getArgument("xDeflection", Float.class),
                ct.getArgument("xDeflectionAfterCollision", Float.class),
                ct.getArgument("zDeflection", Float.class),
                ct.getArgument("zDeflectionAfterCollision", Float.class),
                ct.getArgument("bloomFactor", Float.class),
                metaTag
        );
        return father;
    }

    public static void fastSend(String commandString, CommandSourceStack sourceStack, CommandDispatcher<CommandSourceStack> dispatcher) {
        InheritableCommandDispatcher<CommandSourceStack> inheritableCommandDispatcher = new InheritableCommandDispatcher<>(dispatcher.getRoot());
        ParseResults<CommandSourceStack> parseResults = inheritableCommandDispatcher.parse(new InheritableStringReader(commandString), sourceStack);
        CommandContext<CommandSourceStack> ctPre = parseResults.getContext().build(commandString);
        Collection<ServerPlayer> playerList;
        try {
            playerList = ctPre.getArgument("whoCanSee", EntitySelector.class).findPlayers(ctPre.getSource());
        } catch (CommandSyntaxException e) {
            return;
        }
        CommandContext<CommandSourceStack> ctExe = CommandHelper.getContextHasArgument(ctPre, "targets", EntitySelector.class);
        if (ctExe == null) {
            var meta = ctPre.getArgument("meta", CompoundTag.class);
            boolean canIndex = meta.getBooleanOr(MetaKeys.INDEXED.get(), false);
            var player = sourceStack.getPlayer();
            if (player != null && !T88.TEST) {
                canIndex &= player.getServer() instanceof DedicatedServer;
            }
            if (canIndex) {
                IndexedCommandManager.serverPreform(ctPre, playerList, commandString);
                return;
            }
            sendToAssigned(commandString, sourceStack, playerList, dispatcher);
        } else {
            //execute at/as run mp
            try {
                Collection<? extends ServerPlayer> entities = EntityArgument.getPlayers(ctExe, "targets");
                for (Entity entity : entities) {
                    Thread.startVirtualThread(() -> sendToAssigned(commandString, ctPre.getSource().withEntity(entity).withPosition(entity.position()), playerList, dispatcher));
                }
            } catch (CommandSyntaxException ignored) {
            }
        }
    }

    public static ParseResults<CommandSourceStack> justParse(String commandText) {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        new MadParticleCommand(dispatcher);
        InheritableCommandDispatcher<CommandSourceStack> inheritableDispatcher = new InheritableCommandDispatcher<>(dispatcher.getRoot());
        CommandSourceStack sourceStack = ClientCommandHandler.getSource();
        return inheritableDispatcher.parse(new InheritableStringReader(commandText), sourceStack);
    }
}
