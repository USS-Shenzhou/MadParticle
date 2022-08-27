package cn.ussshenzhou.madparticle.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
public record MadParticleOption(int targetParticle, MadParticle.SpriteFrom spriteFrom, int lifeTime,
                                boolean alwaysRender, int amount,
                                double px, double py, double pz, double xDiffuse, double yDiffuse, double zDiffuse,
                                double vx, double vy, double vz, double vxDiffuse, double vyDiffuse, double vzDiffuse,
                                float friction, float gravity, boolean collision, int bounceTime,
                                double horizontalRelativeCollisionDiffuse, double verticalRelativeCollisionBounce,
                                float afterCollisionFriction, float afterCollisionGravity, boolean interactWithEntity,
                                double horizontalInteractFactor, double verticalInteractFactor,
                                ParticleRenderTypes renderType, float r, float g, float b, float beginAlpha,
                                float endAlpha,
                                MadParticle.ChangeMode alphaMode, float beginScale, float endScale,
                                MadParticle.ChangeMode scaleMode) implements ParticleOptions {
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
            boolean alwaysRender = buf.readBoolean();
            int amount = buf.readInt();
            double px = buf.readDouble(), py = buf.readDouble(), pz = buf.readDouble();
            double xDiffuse = buf.readDouble(), yDiffuse = buf.readDouble(), zDiffuse = buf.readDouble();
            double vx = buf.readDouble(), vy = buf.readDouble(), vz = buf.readDouble();
            double vxDiffuse = buf.readDouble(), vyDiffuse = buf.readDouble(), vzDiffuse = buf.readDouble();
            float friction = buf.readFloat();
            float gravity = buf.readFloat();
            boolean collision = buf.readBoolean();
            int bounceTime = buf.readInt();
            double horizontalRelativeCollisionDiffuse = buf.readDouble(), verticalRelativeCollisionBounce = buf.readDouble();
            float afterCollisionFriction = buf.readFloat();
            float afterCollisionGravity = buf.readFloat();
            boolean interactWithEntity = buf.readBoolean();
            double horizontalInteractFactor = buf.readDouble(), verticalInteractFactor = buf.readDouble();
            ParticleRenderTypes renderType = buf.readEnum(ParticleRenderTypes.class);
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            float beginAlpha = buf.readFloat(), endAlpha = buf.readFloat();
            MadParticle.ChangeMode alphaMode = buf.readEnum(MadParticle.ChangeMode.class);
            float beginScale = buf.readFloat(), endScale = buf.readFloat();
            MadParticle.ChangeMode scaleMode = buf.readEnum(MadParticle.ChangeMode.class);
            return new MadParticleOption(targetParticle, spriteFrom, lifeTime, alwaysRender, amount,
                    px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                    friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                    interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                    renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode
            );
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(targetParticle);
        buf.writeEnum(spriteFrom);
        buf.writeInt(lifeTime);
        buf.writeBoolean(alwaysRender);
        buf.writeInt(amount);
        buf.writeDouble(px);
        buf.writeDouble(py);
        buf.writeDouble(pz);
        buf.writeDouble(xDiffuse);
        buf.writeDouble(yDiffuse);
        buf.writeDouble(zDiffuse);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vy);
        buf.writeDouble(vxDiffuse);
        buf.writeDouble(vyDiffuse);
        buf.writeDouble(vzDiffuse);
        buf.writeFloat(friction);
        buf.writeFloat(gravity);
        buf.writeBoolean(collision);
        buf.writeInt(bounceTime);
        buf.writeDouble(horizontalRelativeCollisionDiffuse);
        buf.writeDouble(verticalRelativeCollisionBounce);
        buf.writeFloat(afterCollisionFriction);
        buf.writeFloat(afterCollisionGravity);
        buf.writeBoolean(interactWithEntity);
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
        }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ModParticleRegistry.MAD_PARTICLE.get();
    }

    @Override
    public @NotNull String writeToString() {
        return "MadParticle";
    }
}
