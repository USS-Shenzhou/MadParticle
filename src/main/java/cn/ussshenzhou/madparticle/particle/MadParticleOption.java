package cn.ussshenzhou.madparticle.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
public class MadParticleOption implements ParticleOptions {
    private final int targetParticle;
    private final MadParticle.SpriteFrom spriteFrom;
    private final int amount;
    private final double px, py, pz;
    private final double xDiffuse, yDiffuse, zDiffuse;
    private final double vx, vy, vz;
    private final double vxDiffuse, vyDiffuse, vzDiffuse;
    private final float friction;
    private final float gravity;
    private final boolean collision;
    private final int bounceTime;
    private final double horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce;
    private final float afterCollisionFriction;
    private final float afterCollisionGravity;
    private final boolean interactWithEntity;
    private final double horizontalInteractFactor, verticalInteractFactor;
    private final int lifeTime;
    private final int renderType;
    private final float r, g, b;
    private final float beginAlpha, endAlpha;
    private final MadParticle.ChangeMode alphaMode;
    private final float beginScale, endScale;
    private final MadParticle.ChangeMode scaleMode;
    private final boolean alwaysRender;


    public static final ParticleOptions.Deserializer<MadParticleOption> DESERIALIZER = new Deserializer<MadParticleOption>() {
        @Override
        public MadParticleOption fromCommand(ParticleType<MadParticleOption> pParticleType, StringReader pReader) throws CommandSyntaxException {
            return null;
        }

        @Override
        public MadParticleOption fromNetwork(ParticleType<MadParticleOption> pParticleType, FriendlyByteBuf buf) {
            int targetParticle = buf.readInt();
            MadParticle.SpriteFrom spriteFrom = buf.readEnum(MadParticle.SpriteFrom.class);
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
            int lifeTime = buf.readInt();
            int renderType = buf.readInt();
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            float beginAlpha = buf.readFloat(), endAlpha = buf.readFloat();
            MadParticle.ChangeMode alphaMode = buf.readEnum(MadParticle.ChangeMode.class);
            float beginScale = buf.readFloat(), endScale = buf.readFloat();
            MadParticle.ChangeMode scaleMode = buf.readEnum(MadParticle.ChangeMode.class);
            boolean alwaysRender = buf.readBoolean();
            return new MadParticleOption(targetParticle, spriteFrom, amount,
                    px, py, pz, xDiffuse, yDiffuse, zDiffuse, vx, vy, vz, vxDiffuse, vyDiffuse, vzDiffuse,
                    friction, gravity, collision, bounceTime, horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce, afterCollisionFriction, afterCollisionGravity,
                    interactWithEntity, horizontalInteractFactor, verticalInteractFactor,
                    lifeTime, renderType, r, g, b, beginAlpha, endAlpha, alphaMode, beginScale, endScale, scaleMode,
                    alwaysRender
                    );
        }
    };

    public MadParticleOption(int targetParticle, MadParticle.SpriteFrom spriteFrom, int amount,
                             double px, double py, double pz, double xDiffuse, double yDiffuse, double zDiffuse,
                             double vx, double vy, double vz, double vxDiffuse, double vyDiffuse, double vzDiffuse,
                             float friction, float gravity, boolean collision, int bounceTime,
                             double horizontalRelativeCollisionDiffuse, double verticalRelativeCollisionBounce, float afterCollisionFriction, float afterCollisionGravity,
                             boolean interactWithEntity, double horizontalInteractFactor, double verticalInteractFactor,
                             int lifeTime, int renderType,
                             float r, float g, float b,
                             float beginAlpha, float endAlpha, MadParticle.ChangeMode alphaMode,
                             float beginScale, float endScale, MadParticle.ChangeMode scaleMode,
                             boolean alwaysRender
    ) {
        this.targetParticle = targetParticle;
        this.spriteFrom = spriteFrom;
        this.amount = amount;
        this.px = px;
        this.py = py;
        this.pz = pz;
        this.xDiffuse = xDiffuse;
        this.yDiffuse = yDiffuse;
        this.zDiffuse = zDiffuse;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.vxDiffuse = vxDiffuse;
        this.vyDiffuse = vyDiffuse;
        this.vzDiffuse = vzDiffuse;
        this.friction = friction;
        this.gravity = gravity;
        this.collision = collision;
        this.bounceTime = bounceTime;
        this.horizontalRelativeCollisionDiffuse = horizontalRelativeCollisionDiffuse;
        this.verticalRelativeCollisionBounce = verticalRelativeCollisionBounce;
        this.afterCollisionFriction = afterCollisionFriction;
        this.afterCollisionGravity = afterCollisionGravity;
        this.interactWithEntity = interactWithEntity;
        this.horizontalInteractFactor = horizontalInteractFactor;
        this.verticalInteractFactor = verticalInteractFactor;
        this.lifeTime = lifeTime;
        this.renderType = renderType;
        this.r = r;
        this.g = g;
        this.b = b;
        this.beginAlpha = beginAlpha;
        this.endAlpha = endAlpha;
        this.alphaMode = alphaMode;
        this.beginScale = beginScale;
        this.endScale = endScale;
        this.scaleMode = scaleMode;
        this.alwaysRender = alwaysRender;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(targetParticle);
        buf.writeEnum(spriteFrom);
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
        buf.writeInt(lifeTime);
        buf.writeInt(renderType);
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(beginAlpha);
        buf.writeFloat(endAlpha);
        buf.writeEnum(alphaMode);
        buf.writeFloat(beginScale);
        buf.writeFloat(endScale);
        buf.writeEnum(scaleMode);
        buf.writeBoolean(alwaysRender);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ModParticleRegistry.MAD_PARTICLE.get();
    }

    @Override
    public @NotNull String writeToString() {
        return "MadParticle";
    }

    public int getTargetParticle() {
        return targetParticle;
    }

    public MadParticle.SpriteFrom getSpriteFrom() {
        return spriteFrom;
    }

    public boolean isAlwaysRender() {
        return alwaysRender;
    }

    public int getAmount() {
        return amount;
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }

    public double getPz() {
        return pz;
    }

    public double getxDiffuse() {
        return xDiffuse;
    }

    public double getyDiffuse() {
        return yDiffuse;
    }

    public double getzDiffuse() {
        return zDiffuse;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public double getVxDiffuse() {
        return vxDiffuse;
    }

    public double getVyDiffuse() {
        return vyDiffuse;
    }

    public double getVzDiffuse() {
        return vzDiffuse;
    }

    public float getFriction() {
        return friction;
    }

    public float getGravity() {
        return gravity;
    }

    public boolean isCollision() {
        return collision;
    }

    public int getBounceTime() {
        return bounceTime;
    }

    public double getHorizontalRelativeCollisionDiffuse() {
        return horizontalRelativeCollisionDiffuse;
    }

    public double getVerticalRelativeCollisionBounce() {
        return verticalRelativeCollisionBounce;
    }

    public float getAfterCollisionFriction() {
        return afterCollisionFriction;
    }

    public float getAfterCollisionGravity() {
        return afterCollisionGravity;
    }

    public boolean isInteractWithEntity() {
        return interactWithEntity;
    }

    public double getHorizontalInteractFactor() {
        return horizontalInteractFactor;
    }

    public double getVerticalInteractFactor() {
        return verticalInteractFactor;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public int getRenderType() {
        return renderType;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getBeginAlpha() {
        return beginAlpha;
    }

    public float getEndAlpha() {
        return endAlpha;
    }

    public MadParticle.ChangeMode getAlphaMode() {
        return alphaMode;
    }

    public float getBeginScale() {
        return beginScale;
    }

    public float getEndScale() {
        return endScale;
    }

    public MadParticle.ChangeMode getScaleMode() {
        return scaleMode;
    }
}
