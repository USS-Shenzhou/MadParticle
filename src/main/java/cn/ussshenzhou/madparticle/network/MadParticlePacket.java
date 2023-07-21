package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ModParticleRegistry;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Random r = new Random();
            if (needAsyncCreate(particleOption.meta())) {
                asyncCreateParticle();
            } else {
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

    private static boolean needAsyncCreate(CompoundTag meta) {
        var keys = meta.tags.keySet();
        return keys.contains("dx")
                || keys.contains("dy")
                || keys.contains("dz")
                ;
    }

    private void asyncCreateParticle() {
        Minecraft mc = Minecraft.getInstance();
        var particleEngine = mc.particleEngine;
        /*CompletableFuture.supplyAsync(() -> {
            var level = mc.level;
            Random r = new Random();
            var providers = ((ParticleEngineAccessor) particleEngine).getProviders();
            //noinspection unchecked
            var provider = ((ParticleProvider<MadParticleOption>) providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleOption.getType())));
            LinkedList<Particle> particles = new LinkedList<>();
            for (int i = 0; i < particleOption.amount(); i++) {
                double x = particleOption.px() + MathHelper.signedRandom(r) * particleOption.xDiffuse();
                double y = particleOption.py() + MathHelper.signedRandom(r) * particleOption.yDiffuse();
                double z = particleOption.pz() + MathHelper.signedRandom(r) * particleOption.zDiffuse();
                if (mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z)
                        > Minecraft.getInstance().options.renderDistance.get() * 4 * Minecraft.getInstance().options.renderDistance.get() * 4) {
                    continue;
                }
                particles.add(
                        provider.createParticle(particleOption, level, x, y, z,
                                particleOption.vx() + MathHelper.signedRandom(r) * particleOption.vxDiffuse(),
                                particleOption.vy() + MathHelper.signedRandom(r) * particleOption.vyDiffuse(),
                                particleOption.vz() + MathHelper.signedRandom(r) * particleOption.vzDiffuse()
                        )
                );
            }
            return particles;
        }).thenAccept(particles -> mc.execute(() -> particles.forEach(particleEngine::add)));*/
        CompletableFuture.runAsync(() -> {
            var level = mc.level;
            Random r = new Random();
            var providers = ((ParticleEngineAccessor) particleEngine).getProviders();
            //noinspection unchecked
            var provider = ((ParticleProvider<MadParticleOption>) providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleOption.getType())));
            LinkedList<Particle> particles = new LinkedList<>();
            for (int i = 0; i < particleOption.amount(); i++) {
                double x = particleOption.px() + MathHelper.signedRandom(r) * particleOption.xDiffuse();
                double y = particleOption.py() + MathHelper.signedRandom(r) * particleOption.yDiffuse();
                double z = particleOption.pz() + MathHelper.signedRandom(r) * particleOption.zDiffuse();
                if (mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z)
                        > Minecraft.getInstance().options.renderDistance.get() * 4 * Minecraft.getInstance().options.renderDistance.get() * 4) {
                    continue;
                }
                particles.add(
                        provider.createParticle(particleOption, level, x, y, z,
                                particleOption.vx() + MathHelper.signedRandom(r) * particleOption.vxDiffuse(),
                                particleOption.vy() + MathHelper.signedRandom(r) * particleOption.vyDiffuse(),
                                particleOption.vz() + MathHelper.signedRandom(r) * particleOption.vzDiffuse()
                        )
                );
            }
            mc.execute(() -> particles.forEach(particleEngine::add));
            //return particles;
        });
    }

}
