package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static cn.ussshenzhou.madparticle.util.MetaKeys.*;

/**
 * @author USS_Shenzhou
 */
public class AddParticleHelper {

    private static Minecraft mc = Minecraft.getInstance();
    private static Random r = new Random();

    public static void addParticle(MadParticleOption option) {
        if (mc.level == null) {
            return;
        }
        if (needAsyncCreate(option.meta())) {
            asyncCreateParticle(option);
        } else {
            syncCreateParticle(option);
        }
    }

    /**
     * @apiNote For caller like Rhino js.
     */
    public static void asyncAddParticle(MadParticleOption option) {
        Minecraft.getInstance().execute(() -> addParticle(option));
    }

    private static boolean needAsyncCreate(CompoundTag meta) {
        return meta.contains("dx")
                || meta.contains("dy")
                || meta.contains("dz")
                || meta.getBoolean(TENET.get())
                || meta.getBoolean(PRE_CAL.get())
                ;
    }

    private static void syncCreateParticle(MadParticleOption option) {
        for (int i = 0; i < option.amount(); i++) {
            mc.level.addParticle(
                    option,
                    option.alwaysRender().get(),
                    fromValueAndDiffuse(option.px(), option.xDiffuse()),
                    fromValueAndDiffuse(option.py(), option.yDiffuse()),
                    fromValueAndDiffuse(option.pz(), option.zDiffuse()),
                    fromValueAndDiffuse(option.vx(), option.vxDiffuse()),
                    fromValueAndDiffuse(option.vy(), option.vyDiffuse()),
                    fromValueAndDiffuse(option.vz(), option.vzDiffuse())
            );
        }
    }

    private static double fromValueAndDiffuse(double value, double diffuse) {
        return value + MathHelper.signedRandom(r) * diffuse;
    }

    private static void asyncCreateParticle(MadParticleOption option) {
        CompletableFuture.runAsync(() -> {
            var particleEngine = mc.particleEngine;
            var level = mc.level;
            var accessor = (ParticleEngineAccessor) particleEngine;
            var providers = accessor.getProviders();
            int particlesCanAdd = ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue - accessor.getParticlesToAdd().size();
            //noinspection unchecked
            var provider = ((ParticleProvider<MadParticleOption>) providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(option.getType())));
            LinkedList<Particle> particles = new LinkedList<>();
            for (int i = 0; i < Math.min(option.amount(), particlesCanAdd); i++) {
                double x = fromValueAndDiffuse(option.px(), option.xDiffuse());
                double y = fromValueAndDiffuse(option.py(), option.yDiffuse());
                double z = fromValueAndDiffuse(option.pz(), option.zDiffuse());
                if (option.alwaysRender().get()) {
                    if (ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance) {
                        if (mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z) > getMaxParticleGenerateDistanceSqr()) {
                            continue;
                        }
                    }
                } else if (mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z) > getNormalParticleGenerateDistanceSqr()) {
                    continue;
                }
                //noinspection DataFlowIssue
                particles.add(
                        provider.createParticle(option, level, x, y, z,
                                fromValueAndDiffuse(option.vx(), option.vxDiffuse()),
                                fromValueAndDiffuse(option.vy(), option.vyDiffuse()),
                                fromValueAndDiffuse(option.vz(), option.vzDiffuse())
                        )
                );
            }
            mc.execute(() -> particles.forEach(particleEngine::add));
        });
    }

    public static int getMaxParticleGenerateDistanceSqr() {
        return 4 * 16 * Minecraft.getInstance().options.renderDistance.get() * 16 * Minecraft.getInstance().options.renderDistance.get();
    }

    public static int getNormalParticleGenerateDistanceSqr() {
        return 4 * Minecraft.getInstance().options.renderDistance.get() * 4 * Minecraft.getInstance().options.renderDistance.get();
    }
}
