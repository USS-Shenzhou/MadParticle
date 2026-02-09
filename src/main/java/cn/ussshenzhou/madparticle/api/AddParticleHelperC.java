package cn.ussshenzhou.madparticle.api;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.TakeOverType;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static cn.ussshenzhou.madparticle.api.AddParticleHelperS.*;

/**
 * @author USS_Shenzhou
 * <br>This file is not subject to the "infectious" restrictions of the GPL-3.0 license,
 * Which means you can call these method like this file is licensed under LGPL.
 * <br>However, you still need to prominently indicate the use of this program as a prerequisite (dependency).
 */
public class AddParticleHelperC {
    private static Minecraft mc = Minecraft.getInstance();

    public static void addParticleClient(MadParticleOption option) {
        if (mc.level == null) {
            return;
        }
        if (needAsyncCreate(option.meta())) {
            asyncCreateParticle(option);
        } else {
            syncCreateParticle(option);
        }
    }

    public static void addParticleClientAsync2Async(MadParticleOption option) {
        if (mc.level == null) {
            return;
        }
        asyncCreateParticle(option);
    }

    public static void addParticleClientAsync2Async(MadParticleOption option, float roll) {
        if (mc.level == null) {
            return;
        }
        asyncCreateParticle(option, roll);
    }

    public static void addParticleClient(ParticleType<?> targetParticle,
                                         SpriteFrom spriteFrom, int lifeTime,
                                         InheritableBoolean alwaysRender, int amount,
                                         double px, double py, double pz, float xDiffuse, float yDiffuse, float zDiffuse,
                                         double vx, double vy, double vz, float vxDiffuse, float vyDiffuse, float vzDiffuse,
                                         float friction, float gravity, InheritableBoolean collision, int bounceTime,
                                         float horizontalRelativeCollisionDiffuse, float verticalRelativeCollisionBounce,
                                         float afterCollisionFriction, float afterCollisionGravity,
                                         InheritableBoolean interactWithEntity,
                                         float horizontalInteractFactor, float verticalInteractFactor,
                                         TakeOverType renderType, float r, float g, float b,
                                         float beginAlpha, float endAlpha, ChangeMode alphaMode,
                                         float beginScale, float endScale, ChangeMode scaleMode,
                                         boolean haveChild, MadParticleOption child,
                                         float rollSpeed,
                                         float xDeflection, float xDeflectionAfterCollision,
                                         float zDeflection, float zDeflectionAfterCollision,
                                         float bloomFactor,
                                         CompoundTag meta) {
        addParticleClient(new MadParticleOption(BuiltInRegistries.PARTICLE_TYPE.getId(targetParticle), spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child,
                rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor, meta)
        );
    }

    /**
     * @apiNote For caller like Rhino js.
     */
    public static void addParticleClientAsync2Default(MadParticleOption option) {
        Minecraft.getInstance().execute(() -> addParticleClient(option));
    }

    private static void syncCreateParticle(MadParticleOption option) {
        for (int i = 0; i < option.amount(); i++) {
            mc.level.addParticle(
                    option,
                    option.alwaysRender().get(),
                    true,
                    fromValueAndDiffuse(option.px(), option.xDiffuse()),
                    fromValueAndDiffuse(option.py(), option.yDiffuse()),
                    fromValueAndDiffuse(option.pz(), option.zDiffuse()),
                    fromValueAndDiffuse(option.vx(), option.vxDiffuse()),
                    fromValueAndDiffuse(option.vy(), option.vyDiffuse()),
                    fromValueAndDiffuse(option.vz(), option.vzDiffuse())
            );
        }
    }

    private static void asyncCreateParticle(MadParticleOption option) {
        CompletableFuture.runAsync(() -> {
            var particleEngine = mc.particleEngine;
            var level = mc.level;
            var accessor = (ParticleEngineAccessor) particleEngine;
            var providers = particleEngine.resourceManager.getProviders();
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
                        if (mc.gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) > getMaxParticleGenerateDistanceSqr()) {
                            continue;
                        }
                    }
                } else if (mc.gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) > getNormalParticleGenerateDistanceSqr()) {
                    continue;
                }
                //noinspection DataFlowIssue
                particles.add(
                        provider.createParticle(option, level, x, y, z,
                                fromValueAndDiffuse(option.vx(), option.vxDiffuse()),
                                fromValueAndDiffuse(option.vy(), option.vyDiffuse()),
                                fromValueAndDiffuse(option.vz(), option.vzDiffuse()),
                                particleEngine.random
                        )
                );
            }
            particleEngine.particlesToAdd.addAll(particles);
        });
    }

    private static void asyncCreateParticle(MadParticleOption option, float roll) {
        CompletableFuture.runAsync(() -> {
            var particleEngine = mc.particleEngine;
            var level = mc.level;
            var accessor = (ParticleEngineAccessor) particleEngine;
            var providers = particleEngine.resourceManager.getProviders();
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
                        if (mc.gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) > getMaxParticleGenerateDistanceSqr()) {
                            continue;
                        }
                    }
                } else if (mc.gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) > getNormalParticleGenerateDistanceSqr()) {
                    continue;
                }
                var p = provider.createParticle(option, level, x, y, z,
                        fromValueAndDiffuse(option.vx(), option.vxDiffuse()),
                        fromValueAndDiffuse(option.vy(), option.vyDiffuse()),
                        fromValueAndDiffuse(option.vz(), option.vzDiffuse()),
                        particleEngine.random
                );
                if (p == null) {
                    continue;
                }
                if (p instanceof SingleQuadParticle singleQuadParticle){
                    singleQuadParticle.roll = singleQuadParticle.oRoll = roll;
                }
                particles.add(p);
            }
            particleEngine.particlesToAdd.addAll(particles);
        });
    }

    public static int getMaxParticleGenerateDistanceSqr() {
        return 16 * 2 * Minecraft.getInstance().options.renderDistance.get() * 16 * 2 * Minecraft.getInstance().options.renderDistance.get();
    }

    public static int getNormalParticleGenerateDistanceSqr() {
        return 16 / 2 * Minecraft.getInstance().options.renderDistance.get() * 16 / 2 * Minecraft.getInstance().options.renderDistance.get();
    }
}
