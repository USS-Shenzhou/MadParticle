package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.command.IndexedCommandManager;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class IndexedCommandPacket {
    public final float x, y, z;
    public final int index;

    public IndexedCommandPacket(float x, float y, float z, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = index;
    }

    @Decoder
    public IndexedCommandPacket(FriendlyByteBuf buf) {
        x = buf.readFloat();
        y = buf.readFloat();
        z = buf.readFloat();
        index = buf.readVarInt();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloatLE(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeVarInt(index);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context) {
        Thread.startVirtualThread(() -> IndexedCommandManager.clientHandle(this));
    }
}
