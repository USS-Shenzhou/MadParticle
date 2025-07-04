package cn.ussshenzhou.madparticle.api;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.network.MadParticlePacket;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;


import java.util.Random;

import static cn.ussshenzhou.madparticle.particle.enums.MetaKeys.*;

/**
 * @author USS_Shenzhou
 * <br>This file is not subject to the "infectious" restrictions of the GPL-3.0 license,
 * Which means you can call these method like this file is licensed under LGPL.
 * <br>However, you still need to prominently indicate the use of this program as a prerequisite (dependency).
 */
public class AddParticleHelperS {
    private static Random r = new Random();

    public static void addParticleServer(ServerLevel level, String command) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        CommandSourceStack source = null;
        if (server == null) {
            return;
        }
        source = new CommandSourceStack(server, Vec3.atLowerCornerOf(level.getSharedSpawnPos()), Vec2.ZERO, level, 4, "Server", Component.literal("Server"), server, null);
        server.getCommands().performPrefixedCommand(source, command);
    }

    public static void addParticleServer(ServerLevel level, MadParticleOption option) {
        level.players().forEach(player -> NetworkHelper.sendToPlayer(player, new MadParticlePacket(option)));
    }

    public static void addParticleServer(ServerPlayer from, MadParticleOption option) {
        NetworkHelper.sendToPlayersTrackingEntityAndSelf(from, new MadParticlePacket(option));
    }

    public static void addParticleServer(ServerLevel level, ParticleType<?> targetParticle,
                                         SpriteFrom spriteFrom, int lifeTime,
                                         InheritableBoolean alwaysRender, int amount,
                                         double px, double py, double pz, float xDiffuse, float yDiffuse, float zDiffuse,
                                         double vx, double vy, double vz, float vxDiffuse, float vyDiffuse, float vzDiffuse,
                                         float friction, float gravity, InheritableBoolean collision, int bounceTime,
                                         float horizontalRelativeCollisionDiffuse, float verticalRelativeCollisionBounce,
                                         float afterCollisionFriction, float afterCollisionGravity,
                                         InheritableBoolean interactWithEntity,
                                         float horizontalInteractFactor, float verticalInteractFactor,
                                         ParticleRenderTypes renderType, float r, float g, float b,
                                         float beginAlpha, float endAlpha, ChangeMode alphaMode,
                                         float beginScale, float endScale, ChangeMode scaleMode,
                                         boolean haveChild, MadParticleOption child,
                                         float rollSpeed,
                                         float xDeflection, float xDeflectionAfterCollision,
                                         float zDeflection, float zDeflectionAfterCollision,
                                         float bloomFactor,
                                         CompoundTag meta) {
        addParticleServer(level, new MadParticleOption(BuiltInRegistries.PARTICLE_TYPE.getId(targetParticle), spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child,
                rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor, meta));
    }

    public static void addParticleServer(ServerPlayer from, ParticleType<?> targetParticle,
                                         SpriteFrom spriteFrom, int lifeTime,
                                         InheritableBoolean alwaysRender, int amount,
                                         double px, double py, double pz, float xDiffuse, float yDiffuse, float zDiffuse,
                                         double vx, double vy, double vz, float vxDiffuse, float vyDiffuse, float vzDiffuse,
                                         float friction, float gravity, InheritableBoolean collision, int bounceTime,
                                         float horizontalRelativeCollisionDiffuse, float verticalRelativeCollisionBounce,
                                         float afterCollisionFriction, float afterCollisionGravity,
                                         InheritableBoolean interactWithEntity,
                                         float horizontalInteractFactor, float verticalInteractFactor,
                                         ParticleRenderTypes renderType, float r, float g, float b,
                                         float beginAlpha, float endAlpha, ChangeMode alphaMode,
                                         float beginScale, float endScale, ChangeMode scaleMode,
                                         boolean haveChild, MadParticleOption child,
                                         float rollSpeed,
                                         float xDeflection, float xDeflectionAfterCollision,
                                         float zDeflection, float zDeflectionAfterCollision,
                                         float bloomFactor,
                                         CompoundTag meta) {
        addParticleServer(from, new MadParticleOption(BuiltInRegistries.PARTICLE_TYPE.getId(targetParticle), spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child,
                rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor, meta));
    }

    protected static boolean needAsyncCreate(CompoundTag meta) {
        return meta.contains(DX.get())
                || meta.contains(DY.get())
                || meta.contains(DZ.get())
                || meta.contains(LIGHT.get())
                || meta.getBooleanOr(TENET.get(), false)
                || meta.getBooleanOr(PRE_CAL.get(), false);
    }

    protected static double fromValueAndDiffuse(double value, double diffuse) {
        return value + MathHelper.signedRandom(r) * diffuse;
    }

    public static int getMaxParticleGenerateDistanceSqr() {
        return 16 * 2 * Minecraft.getInstance().options.renderDistance.get() * 16 * 2 * Minecraft.getInstance().options.renderDistance.get();
    }

    public static int getNormalParticleGenerateDistanceSqr() {
        return 16 / 2 * Minecraft.getInstance().options.renderDistance.get() * 16 / 2 * Minecraft.getInstance().options.renderDistance.get();
    }
}
