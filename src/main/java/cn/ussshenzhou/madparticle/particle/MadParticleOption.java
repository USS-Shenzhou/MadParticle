package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
public record MadParticleOption(int targetParticle, MadParticle.SpriteFrom spriteFrom, int lifeTime,
                                InheritableBoolean alwaysRender, int amount,
                                double px, double py, double pz, double xDiffuse, double yDiffuse, double zDiffuse,
                                double vx, double vy, double vz, double vxDiffuse, double vyDiffuse, double vzDiffuse,
                                float friction, float gravity, InheritableBoolean collision, int bounceTime,
                                double horizontalRelativeCollisionDiffuse, double verticalRelativeCollisionBounce,
                                float afterCollisionFriction, float afterCollisionGravity,
                                InheritableBoolean interactWithEntity,
                                double horizontalInteractFactor, double verticalInteractFactor,
                                ParticleRenderTypes renderType, float r, float g, float b,
                                float beginAlpha, float endAlpha, MadParticle.ChangeMode alphaMode,
                                float beginScale, float endScale, MadParticle.ChangeMode scaleMode,
                                boolean haveChild, MadParticleOption child) implements ParticleOptions {
    public static final Deserializer<MadParticleOption> DESERIALIZER = new Deserializer<MadParticleOption>() {
        @Override
        public MadParticleOption fromCommand(ParticleType<MadParticleOption> pParticleType, StringReader pReader) throws CommandSyntaxException {
            return null;
        }

        @Override
        public MadParticleOption fromNetwork(ParticleType<MadParticleOption> pParticleType, FriendlyByteBuf buf) {
            int targetParticle = buf.readInt();
            MadParticle.SpriteFrom spriteFrom = buf.readEnum(MadParticle.SpriteFrom.class);
            int lifeTime = buf.readInt();
            InheritableBoolean alwaysRender = buf.readEnum(InheritableBoolean.class);
            int amount = buf.readInt();
            double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
            double xDiffuse = buf.readDouble(), yDiffuse = buf.readDouble(), zDiffuse = buf.readDouble();
            double vx = buf.readDouble(), vy = buf.readDouble(), vz = buf.readDouble();
            double vxDiffuse = buf.readDouble(), vyDiffuse = buf.readDouble(), vzDiffuse = buf.readDouble();
            float friction = buf.readFloat();
            float gravity = buf.readFloat();
            InheritableBoolean collision = buf.readEnum(InheritableBoolean.class);
            int bounceTime = buf.readInt();
            double horizontalRelativeCollisionDiffuse = buf.readDouble(), verticalRelativeCollisionBounce = buf.readDouble();
            float afterCollisionFriction = buf.readFloat();
            float afterCollisionGravity = buf.readFloat();
            InheritableBoolean interactWithEntity = buf.readEnum(InheritableBoolean.class);
            double horizontalInteractFactor = buf.readDouble(), verticalInteractFactor = buf.readDouble();
            ParticleRenderTypes renderType = buf.readEnum(ParticleRenderTypes.class);
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            float beginAlpha = buf.readFloat(), endAlpha = buf.readFloat();
            MadParticle.ChangeMode alphaMode = buf.readEnum(MadParticle.ChangeMode.class);
            float beginScale = buf.readFloat(), endScale = buf.readFloat();
            MadParticle.ChangeMode scaleMode = buf.readEnum(MadParticle.ChangeMode.class);
            boolean haveChild = buf.readBoolean();
            MadParticleOption child = haveChild ? MadParticleOption.DESERIALIZER.fromNetwork(ModParticleRegistry.MAD_PARTICLE.get(), buf) : null;
            return new MadParticleOption(targetParticle, spriteFrom, lifeTime, alwaysRender, amount,
                    px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                    friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                    interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                    renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                    haveChild, child
            );
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(targetParticle);
        buf.writeEnum(spriteFrom);
        buf.writeInt(lifeTime);
        buf.writeEnum(alwaysRender);
        buf.writeInt(amount);
        buf.writeDouble(px);
        buf.writeDouble(py);
        buf.writeDouble(pz);
        buf.writeDouble(xDiffuse);
        buf.writeDouble(yDiffuse);
        buf.writeDouble(zDiffuse);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeDouble(vxDiffuse);
        buf.writeDouble(vyDiffuse);
        buf.writeDouble(vzDiffuse);
        buf.writeFloat(friction);
        buf.writeFloat(gravity);
        buf.writeEnum(collision);
        buf.writeInt(bounceTime);
        buf.writeDouble(horizontalRelativeCollisionDiffuse);
        buf.writeDouble(verticalRelativeCollisionBounce);
        buf.writeFloat(afterCollisionFriction);
        buf.writeFloat(afterCollisionGravity);
        buf.writeEnum(interactWithEntity);
        buf.writeDouble(horizontalInteractFactor);
        buf.writeDouble(verticalInteractFactor);
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
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ModParticleRegistry.MAD_PARTICLE.get();
    }

    @Override
    public @NotNull String writeToString() {
        return "MadParticle";
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
                spriteFrom == MadParticle.SpriteFrom.INHERIT ? fatherParticle.spriteFrom : spriteFrom,
                lifeTime == Integer.MAX_VALUE ? fatherParticle.getLifetime() : lifeTime,
                alwaysRender,
                1,
                px == Double.MAX_VALUE ? fatherParticle.getPos().x : px,
                py == Double.MAX_VALUE ? fatherParticle.getPos().y : py,
                pz == Double.MAX_VALUE ? fatherParticle.getPos().z : pz,
                px == Double.MAX_VALUE ? 0 : xDiffuse,
                py == Double.MAX_VALUE ? 0 : yDiffuse,
                pz == Double.MAX_VALUE ? 0 : zDiffuse,
                vx == Double.MAX_VALUE ? fatherParticle.getSpeed().x : vx,
                vy == Double.MAX_VALUE ? fatherParticle.getSpeed().y : vy,
                vz == Double.MAX_VALUE ? fatherParticle.getSpeed().z : vz,
                vx == Double.MAX_VALUE ? 0 : vxDiffuse,
                vy == Double.MAX_VALUE ? 0 : vyDiffuse,
                vz == Double.MAX_VALUE ? 0 : vzDiffuse,
                friction, gravity,
                collision == InheritableBoolean.INHERIT ? InheritableBoolean.wrap(fatherParticle.collision) : collision,
                bounceTime == Integer.MAX_VALUE ? fatherParticle.bounceTime : bounceTime,
                horizontalRelativeCollisionDiffuse == Double.MAX_VALUE ? fatherParticle.horizontalRelativeCollisionDiffuse : horizontalRelativeCollisionDiffuse,
                verticalRelativeCollisionBounce == Double.MAX_VALUE ? fatherParticle.verticalRelativeCollisionBounce : verticalRelativeCollisionBounce,
                afterCollisionFriction, afterCollisionGravity,
                interactWithEntity == InheritableBoolean.INHERIT ? InheritableBoolean.wrap(fatherParticle.interactWithEntity) : interactWithEntity,
                horizontalInteractFactor == Double.MAX_VALUE ? fatherParticle.horizontalInteractFactor : horizontalInteractFactor,
                verticalInteractFactor == Double.MAX_VALUE ? fatherParticle.verticalInteractFactor : verticalInteractFactor,
                renderType,
                r == Float.MAX_VALUE ? fatherParticle.getColor().x() : r,
                g == Float.MAX_VALUE ? fatherParticle.getColor().y() : g,
                b == Float.MAX_VALUE ? fatherParticle.getColor().z() : b,
                beginAlpha, endAlpha,
                alphaMode == MadParticle.ChangeMode.INHERIT ? fatherParticle.alphaMode : alphaMode,
                beginScale, endScale,
                scaleMode == MadParticle.ChangeMode.INHERIT ? fatherParticle.scaleMode : scaleMode,
                haveChild,
                child
        );
    }
}
