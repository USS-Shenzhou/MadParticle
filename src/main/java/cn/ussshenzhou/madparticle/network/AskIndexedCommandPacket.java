package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.command.IndexedCommandManager;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.network.annotation.ServerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class AskIndexedCommandPacket {
    private final int index;

    public AskIndexedCommandPacket(int index) {
        this.index = index;
    }

    @Decoder
    public AskIndexedCommandPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    @ServerHandler
    public void serverHandler(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer p) {
            var command = IndexedCommandManager.getCommand(index);
            if (command != null) {
                NetworkHelper.sendToPlayer(p, new ReplyIndexedCommandPacket(index, command));
            }
        }
    }
}
