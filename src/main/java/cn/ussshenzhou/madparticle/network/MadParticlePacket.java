package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ModParticleRegistry;
import cn.ussshenzhou.madparticle.util.AddParticleHelper;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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
        this.particleOption = MadParticleOption.DESERIALIZER.fromNetwork(ModParticleRegistry.MAD_PARTICLE.get(), buf);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
    }


    @Consumer
    public void handler(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {

        } else {
            clientHandler();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler() {
        AddParticleHelper.addParticleClient(particleOption);
    }

}
