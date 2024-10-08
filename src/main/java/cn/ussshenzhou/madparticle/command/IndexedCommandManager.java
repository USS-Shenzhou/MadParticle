package cn.ussshenzhou.madparticle.command;

import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.network.AskIndexedCommandPacket;
import cn.ussshenzhou.madparticle.network.IndexedCommandPacket;
import cn.ussshenzhou.madparticle.network.ReplyIndexedCommandPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.irisshaders.iris.helpers.Tri;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author USS_Shenzhou
 */
public class IndexedCommandManager {
    private static final BiMap<Integer, String> INDEXED_COMMANDS = Maps.synchronizedBiMap(HashBiMap.create());
    private static final ConcurrentHashMap<Integer, IndexedCommandPacket> CLIENT_BUFFER = new ConcurrentHashMap<>();
    private static final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

    static {
        //noinspection InstantiationOfUtilityClass
        new MadParticleCommand(dispatcher);
    }

    public static void serverPreform(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, String command) {
        int index;
        if (INDEXED_COMMANDS.containsValue(command)) {
            index = INDEXED_COMMANDS.inverse().get(command);
        } else {
            index = ThreadLocalRandom.current().nextInt();
            INDEXED_COMMANDS.put(index, command);
        }
        var pos = context.getSource().getPosition();
        players.forEach(player -> NetworkHelper.sendToPlayer(player, new IndexedCommandPacket(pos.x, pos.y, pos.z, index)));
    }

    public static void clientHandle(IndexedCommandPacket packet) {
        if (INDEXED_COMMANDS.get(packet.index) == null) {
            NetworkHelper.sendToServer(new AskIndexedCommandPacket(packet.index));
            CLIENT_BUFFER.put(packet.index, packet);
            return;
        }
        preform(packet.x, packet.y, packet.z, INDEXED_COMMANDS.get(packet.index));
    }

    public static void clientHandle(ReplyIndexedCommandPacket reply) {
        var packet = CLIENT_BUFFER.get(reply.index);
        if (packet == null) {
            return;
        }
        CLIENT_BUFFER.remove(packet.index);
        INDEXED_COMMANDS.put(reply.index, reply.command);
        preform(packet.x, packet.y, packet.z, reply.command);
    }

    public static void preform(double x, double y, double z, String command) {
        var option = MadParticleCommand.assembleOption(command, Minecraft.getInstance().player.createCommandSourceStack()
                        .withPosition(new Vec3(x, y, z)),
                dispatcher);
        AddParticleHelper.addParticleClientAsync2Async(option);
    }

    public static String getCommand(int index) {
        return INDEXED_COMMANDS.get(index);
    }
}
