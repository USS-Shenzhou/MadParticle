package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.madparticle.util.MetaKeys;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("DuplicatedCode")
public record MadParticleOption(int targetParticle, SpriteFrom spriteFrom, int lifeTime,
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
                                CompoundTag meta

) implements ParticleOptions {
    public static final MapCodec<MadParticleOption> MAP_CODEC = MapCodec.unit(null);
    public static final StreamCodec<? super RegistryFriendlyByteBuf, MadParticleOption> STREAM_CODEC = StreamCodec.ofMember(MadParticleOption::writeToNetwork, MadParticleOption::fromNetwork);

    public static MadParticleOption fromNetwork(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return fromNetworkF16(buf);
        } else {
            return fromNetworkF32(buf);
        }
    }

    private static @NotNull MadParticleOption fromNetworkF16(FriendlyByteBuf buf) {
        int targetParticle = buf.readVarInt();
        SpriteFrom spriteFrom = buf.readEnum(SpriteFrom.class);
        int lifeTime = buf.readVarInt();
        InheritableBoolean alwaysRender = buf.readEnum(InheritableBoolean.class);
        int amount = buf.readVarInt();
        double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
        float xDiffuse = buf.readFloat(), yDiffuse = buf.readFloat(), zDiffuse = buf.readFloat();
        double vx = buf.readDouble(), vy = buf.readDouble(), vz = buf.readDouble();
        float vxDiffuse = Float.float16ToFloat(buf.readShort()), vyDiffuse = Float.float16ToFloat(buf.readShort()), vzDiffuse = Float.float16ToFloat(buf.readShort());
        float friction = Float.float16ToFloat(buf.readShort());
        float gravity = Float.float16ToFloat(buf.readShort());
        InheritableBoolean collision = buf.readEnum(InheritableBoolean.class);
        int bounceTime = buf.readVarInt();
        float horizontalRelativeCollisionDiffuse = Float.float16ToFloat(buf.readShort()), verticalRelativeCollisionBounce = Float.float16ToFloat(buf.readShort());
        float afterCollisionFriction = Float.float16ToFloat(buf.readShort());
        float afterCollisionGravity = Float.float16ToFloat(buf.readShort());
        InheritableBoolean interactWithEntity = buf.readEnum(InheritableBoolean.class);
        float horizontalInteractFactor = Float.float16ToFloat(buf.readShort()), verticalInteractFactor = Float.float16ToFloat(buf.readShort());
        ParticleRenderTypes renderType = buf.readEnum(ParticleRenderTypes.class);
        float r = Float.float16ToFloat(buf.readShort()), g = Float.float16ToFloat(buf.readShort()), b = Float.float16ToFloat(buf.readShort());
        float beginAlpha = Float.float16ToFloat(buf.readShort()), endAlpha = Float.float16ToFloat(buf.readShort());
        ChangeMode alphaMode = buf.readEnum(ChangeMode.class);
        float beginScale = Float.float16ToFloat(buf.readShort()), endScale = Float.float16ToFloat(buf.readShort());
        ChangeMode scaleMode = buf.readEnum(ChangeMode.class);
        boolean haveChild = buf.readBoolean();
        MadParticleOption child = haveChild ? fromNetwork(buf) : null;
        float rollSpeed = Float.float16ToFloat(buf.readShort());
        float xDeflection = Float.float16ToFloat(buf.readShort());
        float xDeflectionAfterCollision = Float.float16ToFloat(buf.readShort());
        float zDeflection = Float.float16ToFloat(buf.readShort());
        float zDeflectionAfterCollision = Float.float16ToFloat(buf.readShort());
        float bloomFactor = Float.float16ToFloat(buf.readShort());
        CompoundTag meta = buf.readNbt();
        return new MadParticleOption(targetParticle, spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child,
                rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor, meta
        );
    }

    private static @NotNull MadParticleOption fromNetworkF32(FriendlyByteBuf buf) {
        int targetParticle = buf.readVarInt();
        SpriteFrom spriteFrom = buf.readEnum(SpriteFrom.class);
        int lifeTime = buf.readVarInt();
        InheritableBoolean alwaysRender = buf.readEnum(InheritableBoolean.class);
        int amount = buf.readVarInt();
        double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
        float xDiffuse = buf.readFloat(), yDiffuse = buf.readFloat(), zDiffuse = buf.readFloat();
        double vx = buf.readDouble(), vy = buf.readDouble(), vz = buf.readDouble();
        float vxDiffuse = buf.readFloat(), vyDiffuse = buf.readFloat(), vzDiffuse = buf.readFloat();
        float friction = buf.readFloat();
        float gravity = buf.readFloat();
        InheritableBoolean collision = buf.readEnum(InheritableBoolean.class);
        int bounceTime = buf.readVarInt();
        float horizontalRelativeCollisionDiffuse = buf.readFloat(), verticalRelativeCollisionBounce = buf.readFloat();
        float afterCollisionFriction = buf.readFloat();
        float afterCollisionGravity = buf.readFloat();
        InheritableBoolean interactWithEntity = buf.readEnum(InheritableBoolean.class);
        float horizontalInteractFactor = buf.readFloat(), verticalInteractFactor = buf.readFloat();
        ParticleRenderTypes renderType = buf.readEnum(ParticleRenderTypes.class);
        float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
        float beginAlpha = buf.readFloat(), endAlpha = buf.readFloat();
        ChangeMode alphaMode = buf.readEnum(ChangeMode.class);
        float beginScale = buf.readFloat(), endScale = buf.readFloat();
        ChangeMode scaleMode = buf.readEnum(ChangeMode.class);
        boolean haveChild = buf.readBoolean();
        MadParticleOption child = haveChild ? fromNetwork(buf) : null;
        float rollSpeed = buf.readFloat();
        float xDeflection = buf.readFloat();
        float xDeflectionAfterCollision = buf.readFloat();
        float zDeflection = buf.readFloat();
        float zDeflectionAfterCollision = buf.readFloat();
        float bloomFactor = buf.readFloat();
        CompoundTag meta = buf.readNbt();
        return new MadParticleOption(targetParticle, spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child,
                rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor, meta
        );
    }


    public void writeToNetwork(FriendlyByteBuf buf) {
        if (meta.getBoolean(MetaKeys.HALF_PRECISION.get())) {
            buf.writeBoolean(true);
            write2NetworkF16(buf);
        } else {
            buf.writeBoolean(false);
            write2NetworkF32(buf);
        }
    }

    private void write2NetworkF16(FriendlyByteBuf buf) {
        buf.writeVarInt(targetParticle);
        buf.writeEnum(spriteFrom);
        buf.writeVarInt(lifeTime);
        buf.writeEnum(alwaysRender);
        buf.writeVarInt(amount);
        buf.writeDouble(px);
        buf.writeDouble(py);
        buf.writeDouble(pz);
        buf.writeFloat(xDiffuse);
        buf.writeFloat(yDiffuse);
        buf.writeFloat(zDiffuse);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeShort(Float.floatToFloat16(vxDiffuse));
        buf.writeShort(Float.floatToFloat16(vyDiffuse));
        buf.writeShort(Float.floatToFloat16(vzDiffuse));
        buf.writeShort(Float.floatToFloat16(friction));
        buf.writeShort(Float.floatToFloat16(gravity));
        buf.writeEnum(collision);
        buf.writeVarInt(bounceTime);
        buf.writeShort(Float.floatToFloat16(horizontalRelativeCollisionDiffuse));
        buf.writeShort(Float.floatToFloat16(verticalRelativeCollisionBounce));
        buf.writeShort(Float.floatToFloat16(afterCollisionFriction));
        buf.writeShort(Float.floatToFloat16(afterCollisionGravity));
        buf.writeEnum(interactWithEntity);
        buf.writeShort(Float.floatToFloat16(horizontalInteractFactor));
        buf.writeShort(Float.floatToFloat16(verticalInteractFactor));
        buf.writeEnum(renderType);
        buf.writeShort(Float.floatToFloat16(r));
        buf.writeShort(Float.floatToFloat16(g));
        buf.writeShort(Float.floatToFloat16(b));
        buf.writeShort(Float.floatToFloat16(beginAlpha));
        buf.writeShort(Float.floatToFloat16(endAlpha));
        buf.writeEnum(alphaMode);
        buf.writeShort(Float.floatToFloat16(beginScale));
        buf.writeShort(Float.floatToFloat16(endScale));
        buf.writeEnum(scaleMode);
        buf.writeBoolean(haveChild);
        if (haveChild) {
            child.writeToNetwork(buf);
        }
        buf.writeShort(Float.floatToFloat16(rollSpeed));
        buf.writeShort(Float.floatToFloat16(xDeflection));
        buf.writeShort(Float.floatToFloat16(xDeflectionAfterCollision));
        buf.writeShort(Float.floatToFloat16(zDeflection));
        buf.writeShort(Float.floatToFloat16(zDeflectionAfterCollision));
        buf.writeShort(Float.floatToFloat16(bloomFactor));
        buf.writeNbt(meta);
    }

    private void write2NetworkF32(FriendlyByteBuf buf) {
        buf.writeVarInt(targetParticle);
        buf.writeEnum(spriteFrom);
        buf.writeVarInt(lifeTime);
        buf.writeEnum(alwaysRender);
        buf.writeVarInt(amount);
        buf.writeDouble(px);
        buf.writeDouble(py);
        buf.writeDouble(pz);
        buf.writeFloat(xDiffuse);
        buf.writeFloat(yDiffuse);
        buf.writeFloat(zDiffuse);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeFloat(vxDiffuse);
        buf.writeFloat(vyDiffuse);
        buf.writeFloat(vzDiffuse);
        buf.writeFloat(friction);
        buf.writeFloat(gravity);
        buf.writeEnum(collision);
        buf.writeVarInt(bounceTime);
        buf.writeFloat(horizontalRelativeCollisionDiffuse);
        buf.writeFloat(verticalRelativeCollisionBounce);
        buf.writeFloat(afterCollisionFriction);
        buf.writeFloat(afterCollisionGravity);
        buf.writeEnum(interactWithEntity);
        buf.writeFloat(horizontalInteractFactor);
        buf.writeFloat(verticalInteractFactor);
        buf.writeEnum(renderType);
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(beginAlpha);
        buf.writeFloat(endAlpha);
        buf.writeEnum(alphaMode);
        buf.writeFloat(beginScale);
        buf.writeFloat(endScale);
        buf.writeEnum(scaleMode);
        buf.writeBoolean(haveChild);
        if (haveChild) {
            child.writeToNetwork(buf);
        }
        buf.writeFloat(rollSpeed);
        buf.writeFloat(xDeflection);
        buf.writeFloat(xDeflectionAfterCollision);
        buf.writeFloat(zDeflection);
        buf.writeFloat(zDeflectionAfterCollision);
        buf.writeFloat(bloomFactor);
        buf.writeNbt(meta);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ModParticleTypeRegistry.MAD_PARTICLE.get();
    }

    /*public MadParticleOption copy(){
        return new MadParticleOption(targetParticle, spriteFrom, lifeTime, alwaysRender, amount,
                px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                haveChild, child);
    }*/

    public MadParticleOption inheritOrContinue(MadParticle fatherParticle) {
        return new MadParticleOption(
                targetParticle,
                spriteFrom == SpriteFrom.INHERIT ? fatherParticle.spriteFrom : spriteFrom,
                lifeTime == Integer.MAX_VALUE ? fatherParticle.getLifetime() : lifeTime,
                alwaysRender,
                1,
                px == Double.MAX_VALUE ? fatherParticle.getPos().x : px,
                py == Double.MAX_VALUE ? fatherParticle.getPos().y : py,
                pz == Double.MAX_VALUE ? fatherParticle.getPos().z : pz,
                xDiffuse,
                yDiffuse,
                zDiffuse,
                vx == Double.MAX_VALUE ? fatherParticle.getSpeed().x : vx,
                vy == Double.MAX_VALUE ? fatherParticle.getSpeed().y : vy,
                vz == Double.MAX_VALUE ? fatherParticle.getSpeed().z : vz,
                vxDiffuse,
                vyDiffuse,
                vzDiffuse,
                friction, gravity,
                collision == InheritableBoolean.INHERIT ? InheritableBoolean.wrap(fatherParticle.collision) : collision,
                bounceTime == Integer.MAX_VALUE ? fatherParticle.bounceTime : bounceTime,
                horizontalRelativeCollisionDiffuse == Float.MAX_VALUE ? fatherParticle.horizontalRelativeCollisionDiffuse : horizontalRelativeCollisionDiffuse,
                verticalRelativeCollisionBounce == Float.MAX_VALUE ? fatherParticle.verticalRelativeCollisionBounce : verticalRelativeCollisionBounce,
                afterCollisionFriction, afterCollisionGravity,
                interactWithEntity == InheritableBoolean.INHERIT ? InheritableBoolean.wrap(fatherParticle.interactWithEntity) : interactWithEntity,
                horizontalInteractFactor == Float.MAX_VALUE ? fatherParticle.horizontalInteractFactor : horizontalInteractFactor,
                verticalInteractFactor == Float.MAX_VALUE ? fatherParticle.verticalInteractFactor : verticalInteractFactor,
                renderType,
                r == Float.MAX_VALUE ? fatherParticle.getColor().x() : r,
                g == Float.MAX_VALUE ? fatherParticle.getColor().y() : g,
                b == Float.MAX_VALUE ? fatherParticle.getColor().z() : b,
                beginAlpha, endAlpha,
                alphaMode == ChangeMode.INHERIT ? fatherParticle.alphaMode : alphaMode,
                beginScale, endScale,
                scaleMode == ChangeMode.INHERIT ? fatherParticle.scaleMode : scaleMode,
                haveChild,
                child,
                rollSpeed == Float.MAX_VALUE ? fatherParticle.rollSpeed : rollSpeed,
                xDeflection, xDeflectionAfterCollision, zDeflection, zDeflectionAfterCollision,
                bloomFactor == Float.MAX_VALUE ? fatherParticle.bloomFactor : bloomFactor,
                meta
        );
    }
}
