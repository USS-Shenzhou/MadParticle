package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ModParticleRegistry;
import cn.ussshenzhou.madparticle.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

import java.util.Random;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class MadParticlePacket {
    private final MadParticleOption particleOption;

    public MadParticlePacket(MadParticleOption particleOption) {
        this.particleOption = particleOption;
    }

    public MadParticlePacket(FriendlyByteBuf buf) {
        this.particleOption = MadParticleOption.DESERIALIZER.fromNetwork(ModParticleRegistry.MAD_PARTICLE.get(), buf);
    }

    public void write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
    }

    public void handler(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(
                () -> {
                    if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {

                    } else {
                        clientHandler();
                    }
                }
        );
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler() {
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

}
