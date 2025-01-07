package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.command.IndexedCommandManager;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class ReplyIndexedCommandPacket {
    public final int index;
    public final String command;

    public ReplyIndexedCommandPacket(int index, String command) {
        if (command == null) {
            LogUtils.getLogger().error("command is null!");
        }
        this.index = index;
        this.command = command;
    }

    @Decoder
    public ReplyIndexedCommandPacket(FriendlyByteBuf buf) {
        this.index = buf.readVarInt();
        this.command = buf.readUtf();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeUtf(command);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context){
        Thread.startVirtualThread(()-> IndexedCommandManager.clientHandle(this));
    }
}
