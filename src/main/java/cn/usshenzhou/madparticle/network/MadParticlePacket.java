package cn.usshenzhou.madparticle.network;

import cn.usshenzhou.madparticle.Madparticle;
import cn.usshenzhou.madparticle.particle.MadParticleOption;
import cn.usshenzhou.madparticle.particle.MadParticleShader;
import cn.usshenzhou.madparticle.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * @author USS_Shenzhou
 */
public class MadParticlePacket {
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Madparticle.MOD_ID,"mad_particle_packet");

    private final MadParticleOption particleOption;

    public MadParticlePacket(MadParticleOption particleOption) {
        this.particleOption = particleOption;
    }

    public MadParticlePacket(FriendlyByteBuf buf) {
        this.particleOption = MadParticleOption.DESERIALIZER.fromNetwork(Madparticle.MAD_PARTICLE, buf);
    }

    public FriendlyByteBuf write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
        return buf;
    }

    public void clientHandler() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Random r = new Random();
            for (int i = 0; i < particleOption.amount(); i++) {
                level.addParticle(
                        particleOption,
                        particleOption.alwaysRender().get(),
                        particleOption.px() + MathHelper.signedRandom(r) * particleOption.xDiffuse(),
                        particleOption.py() + MathHelper.signedRandom(r) * particleOption.yDiffuse(),
                        particleOption.pz() + MathHelper.signedRandom(r) * particleOption.zDiffuse(),
                        particleOption.vx() + MathHelper.signedRandom(r) * particleOption.vxDiffuse(),
                        particleOption.vy() + MathHelper.signedRandom(r) * particleOption.vyDiffuse(),
                        particleOption.vz() + MathHelper.signedRandom(r) * particleOption.vzDiffuse()
                );
            }
        }
    }

    public MadParticleOption getParticleOption() {
        return particleOption;
    }
}
