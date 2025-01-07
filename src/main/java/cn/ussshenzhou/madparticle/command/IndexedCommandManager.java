package cn.ussshenzhou.madparticle.command;

import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.network.AskIndexedCommandPacket;
import cn.ussshenzhou.madparticle.network.IndexedCommandPacket;
import cn.ussshenzhou.madparticle.network.ReplyIndexedCommandPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author USS_Shenzhou
 */
public class IndexedCommandManager {
    private static final BiMap<Integer, String> INDEXED_COMMANDS = Maps.synchronizedBiMap(HashBiMap.create());
    private static final ConcurrentHashMap<Integer, IndexedCommandPacket> CLIENT_BUFFER = new ConcurrentHashMap<>();
    private static final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    private static final ConcurrentHashMultiset<Integer> ASKED_IDS = ConcurrentHashMultiset.create();

    static {
        //noinspection InstantiationOfUtilityClass
        new MadParticleCommand(dispatcher);
    }

    public static void serverPreform(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, String command) {
        synchronized (INDEXED_COMMANDS) {
            int index = INDEXED_COMMANDS.inverse().computeIfAbsent(command, c -> {
                int i;
                do {
                    i = randomIndex();
                } while (INDEXED_COMMANDS.containsKey(i));
                return i;
            });
            var pos = context.getSource().getPosition();
            players.forEach(player -> NetworkHelper.sendToPlayer(player, new IndexedCommandPacket((float) pos.x, (float) pos.y, (float) pos.z, index)));
        }
    }

    private static int randomIndex() {
        int index;
        if (INDEXED_COMMANDS.size() < 512) {
            index = ThreadLocalRandom.current().nextInt(16384);
        } else if (INDEXED_COMMANDS.size() < 65536) {
            index = ThreadLocalRandom.current().nextInt(2097152);
        } else {
            index = ThreadLocalRandom.current().nextInt();
        }
        return index;
    }

    public static void clientHandle(IndexedCommandPacket packet) {
        if (INDEXED_COMMANDS.get(packet.index) == null && !ASKED_IDS.contains(packet.index)) {
            NetworkHelper.sendToServer(new AskIndexedCommandPacket(packet.index));
            CLIENT_BUFFER.put(packet.index, packet);
            ASKED_IDS.add(packet.index);
            return;
        }
        preform(packet.x, packet.y, packet.z, INDEXED_COMMANDS.get(packet.index));
    }

    public static void clientHandle(ReplyIndexedCommandPacket reply) {
        synchronized (INDEXED_COMMANDS) {
            var packet = CLIENT_BUFFER.get(reply.index);
            if (packet == null) {
                return;
            }
            if (INDEXED_COMMANDS.get(reply.index) != null) {
                preform(packet.x, packet.y, packet.z, INDEXED_COMMANDS.get(packet.index));
                return;
            }
            CLIENT_BUFFER.remove(packet.index);
            if (!INDEXED_COMMANDS.containsValue(reply.command)) {
                INDEXED_COMMANDS.put(reply.index, reply.command);
            }
            preform(packet.x, packet.y, packet.z, reply.command);
        }
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

    public static void clear() {
        synchronized (INDEXED_COMMANDS) {
            INDEXED_COMMANDS.clear();
        }
        ASKED_IDS.clear();
        CLIENT_BUFFER.clear();
    }
}
