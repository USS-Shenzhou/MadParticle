package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * @author USS_Shenzhou
 */
public class MadParticlePacketSend {
    public static SimpleChannel CHANNEL;

    public static String VERSION = "1.0";
    private static int id = 0;

    public static int nextId() {
        return id++;
    }

    public static void registerMessage() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MadParticle.MOD_ID, "particle_packet"),
                () -> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
        CHANNEL.messageBuilder(MadParticlePacket.class, nextId())
                .encoder(MadParticlePacket::write)
                .decoder(MadParticlePacket::new)
                .consumerMainThread(MadParticlePacket::handler)
                .add();
    }
}
