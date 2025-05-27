package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.api.AddParticleHelperC;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
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
public class MadParticlePacket {
    private final MadParticleOption particleOption;

    public MadParticlePacket(MadParticleOption particleOption) {
        this.particleOption = particleOption;
    }

    @Decoder
    public MadParticlePacket(FriendlyByteBuf buf) {
        this.particleOption = MadParticleOption.fromNetwork(buf);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context) {
        AddParticleHelperC.addParticleClientAsync2Async(particleOption);
    }

}
