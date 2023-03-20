package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.util.MathHelper;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public class MadParticle extends TextureSheetParticle {
    protected final SpriteSet sprites;
    protected final SpriteFrom spriteFrom;
    protected final float beginGravity;
    protected final boolean collision;
    protected final int bounceTime;
    protected final double horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce;
    protected final float afterCollisionFriction;
    protected final float afterCollisionGravity;
    protected final boolean interactWithEntity;
    protected final double horizontalInteractFactor, verticalInteractFactor;
    protected final ParticleRenderType particleRenderType;
    protected final float beginAlpha, endAlpha;
    protected final ChangeMode alphaMode;
    protected final float beginScale, endScale;
    protected final ChangeMode scaleMode;
    protected final MadParticleOption child;
    protected final float rollSpeed;
    protected float xDeflection;
    protected float zDeflection;
    protected final float xDeflectionAfterCollision;
    protected final float zDeflectionAfterCollision;
    protected final float bloomFactor;

    private int bounceCount = 0;
    private float scale;
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);
    private static final float MAX_DIRECTIONAL_LOSS = 0.65f;
    protected final float xDeflectionInitial;
    protected final float zDeflectionInitial;
    protected final float frictionInitial;

    @SuppressWarnings("AlibabaSwitchStatement")
    public MadParticle(ClientLevel pLevel, SpriteSet spriteSet, SpriteFrom spriteFrom,
                       double pX, double pY, double pZ, double vx, double vy, double vz,
                       float friction, float gravity, boolean collision, int bounceTime,
                       double horizontalRelativeCollisionDiffuse, double verticalRelativeCollisionBounce, float afterCollisionFriction, float afterCollisionGravity,
                       boolean interactWithEntity, double horizontalInteractFactor, double verticalInteractFactor,
                       int lifeTime, ParticleRenderType renderType,
                       float r, float g, float b,
                       float beginAlpha, float endAlpha, ChangeMode alphaMode,
                       float beginScale, float endScale, ChangeMode scaleMode,
                       MadParticleOption child,
                       float rollSpeed,
                       float xDeflection, float xDeflectionAfterCollision, float zDeflection, float zDeflectionAfterCollision,
                       float bloomFactor
    ) {
        super(pLevel, pX, pY, pZ);
        this.sprites = spriteSet;
        this.spriteFrom = spriteFrom;
        switch (spriteFrom) {
            case AGE -> this.setSpriteFromAge(spriteSet);
            default -> this.pickSprite(spriteSet);
        }
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = friction;
        this.frictionInitial = friction;
        this.beginGravity = gravity;
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
        this.lifetime = (int) (lifeTime * (1 + 0.1 * MathHelper.signedRandom()));
        this.particleRenderType = renderType;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.beginAlpha = beginAlpha;
        this.alpha = beginAlpha;
        this.endAlpha = endAlpha;
        this.alphaMode = alphaMode;
        this.beginScale = beginScale;
        this.setSize(0.2f, 0.2f);
        this.scale(beginScale);
        this.scale = beginScale;
        this.endScale = endScale;
        this.scaleMode = scaleMode;
        this.hasPhysics = true;
        this.child = child;
        this.rollSpeed = (float) (rollSpeed * (1 + 0.1 * MathHelper.signedRandom()));
        if (rollSpeed != 0) {
            this.roll = (float) (Math.random() * Math.PI * 2);
        } else {
            this.roll = 0;
        }
        this.xDeflectionInitial = xDeflection;
        this.xDeflection = xDeflection;
        this.zDeflectionInitial = zDeflection;
        this.zDeflection = zDeflection;
        this.xDeflectionAfterCollision = xDeflectionAfterCollision;
        this.zDeflectionAfterCollision = zDeflectionAfterCollision;
        this.bloomFactor = bloomFactor;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            //interact with Entity
            if (interactWithEntity) {
                LivingEntity entity = level.getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat().range(4), null, x, y, z, this.getBoundingBox().inflate(0.7));
                if (entity != null) {
                    Vec3 v = entity.getDeltaMovement();
                    this.xd += v.x * random.nextFloat() * horizontalInteractFactor;
                    double dy = Math.max(Math.abs(v.y * verticalInteractFactor), Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.z, 2)) * verticalInteractFactor);
                    if (dy != 0) {
                        this.onGround = false;
                    }
                    this.yd += (v.y < 0 ? -dy : dy);
                    this.zd += v.z * random.nextFloat() * horizontalInteractFactor;
                    this.gravity = beginGravity;
                    this.friction = frictionInitial;
                    this.xDeflection = xDeflectionInitial;
                    this.zDeflection = zDeflectionInitial;
                }
            }
            //gravity and deflection
            this.yd -= 0.04 * (double) this.gravity;
            this.xd += 0.04 * this.xDeflection;
            this.zd += 0.04 * this.zDeflection;
            //normal
            this.move(this.xd, this.yd, this.zd);
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            //sprite
            if (this.spriteFrom == SpriteFrom.AGE) {
                this.setSpriteFromAge(sprites);
            }
            //alpha
            this.alpha = alphaMode.lerp(beginAlpha, endAlpha, age, lifetime);
            //size
            this.scale(1 / scale);
            scale = scaleMode.lerp(beginScale, endScale, age, lifetime);
            this.scale(scale);
            //roll
            this.oRoll = this.roll;
            if (!this.onGround) {
                this.roll += (float) Math.PI * rollSpeed * 2.0F;
            }
        }
    }

    @SuppressWarnings("AlibabaAvoidDoubleOrFloatEqualCompare")
    @Override
    public void move(double pX, double pY, double pZ) {
        double x0 = pX;
        double y0 = pY;
        double z0 = pZ;
        double r2 = pX * pX + pY * pY + pZ * pZ;
        if (collision && this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D) && r2 < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 vec3 = Entity.collideBoundingBox((Entity) null, new Vec3(pX, pY, pZ), this.getBoundingBox(), this.level, List.of());
            pX = vec3.x;
            pY = vec3.y;
            pZ = vec3.z;
        }
        if (pX != 0.0D || pY != 0.0D || pZ != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
            this.setLocationFromBoundingbox();
        }
        if (collision) {
            //hit XOZ
            if (y0 != pY) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, xd, zd);
                    this.xd = v.x;
                    this.yd = -y0 * (random.nextDouble() * verticalRelativeCollisionBounce);
                    this.zd = v.y;
                    updateGravityAndDeflection();
                    bounceCount++;
                } else {
                    this.gravity = 0;
                    this.onGround = true;
                }
                this.friction = afterCollisionFriction;
                return;
            }
            //hit YOZ
            if (x0 != pX) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, yd, zd);
                    this.xd = -x0 * (random.nextDouble() * verticalRelativeCollisionBounce);
                    this.yd = v.x;
                    this.zd = v.y;
                    updateGravityAndDeflection();
                    bounceCount++;
                }
                this.friction = afterCollisionFriction;
                return;
            }
            //hit XOY
            if (z0 != pZ) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, xd, yd);
                    this.xd = v.x;
                    this.yd = v.y;
                    this.zd = -z0 * (random.nextDouble() * verticalRelativeCollisionBounce);
                    updateGravityAndDeflection();
                    bounceCount++;
                }
                this.friction = afterCollisionFriction;
                return;
            }
        }
    }

    private void updateGravityAndDeflection() {
        this.gravity = afterCollisionGravity;
        this.xDeflection = xDeflectionAfterCollision;
        this.zDeflection = zDeflectionAfterCollision;
    }

    public Vec2 horizontalRelativeCollision(double r2, double d1, double d2) {
        //generalLoss controls radius of spread circle.
        r2 *= horizontalRelativeCollisionDiffuse;
        float r = (float) Math.sqrt(r2);
        float a = (float) Math.random() * r * (random.nextBoolean() ? -1 : 1);
        float b = (float) Math.sqrt(r2 - a * a) * (random.nextBoolean() ? -1 : 1);
        //lose energy/speed when bouncing to different directions.
        //lose less speed when going forward. lose more speed when going backward.
        float d = (float) Math.sqrt((d1 - a) * (d1 - a) + (d2 - b) * (d2 - b));
        float directionalLoss = 1 - d / (2 * r) * MAX_DIRECTIONAL_LOSS;
        return new Vec2((float) (a * directionalLoss * Math.random()), (float) (b * directionalLoss * Math.random()));
    }

    @Override
    public void remove() {
        super.remove();
        if (this.child != null) {
            MadParticleOption p = child.inheritOrContinue(this);
            Minecraft.getInstance().level.addParticle(
                    p,
                    p.alwaysRender().get(),
                    p.px() + MathHelper.signedRandom(random) * p.xDiffuse(),
                    p.py() + MathHelper.signedRandom(random) * p.yDiffuse(),
                    p.pz() + MathHelper.signedRandom(random) * p.zDiffuse(),
                    p.vx() + MathHelper.signedRandom(random) * p.vxDiffuse(),
                    p.vy() + MathHelper.signedRandom(random) * p.vyDiffuse(),
                    p.vz() + MathHelper.signedRandom(random) * p.vzDiffuse()
            );
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return particleRenderType;
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public Vec3 getSpeed() {
        return new Vec3(xd, yd, zd);
    }

    public Vector3f getColor() {
        return new Vector3f(rCol, gCol, bCol);
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        if (particleRenderType instanceof MadParticleRenderTypes) {
            MadParticleBufferBuilder buffer = ((MadParticleRenderTypes) (particleRenderType)).bufferBuilder;
            //copied from SingleQuadParticle.render for compatability with Rubidium
            Vec3 vec3 = pRenderInfo.getPosition();
            float f = (float) (Mth.lerp((double) pPartialTicks, this.xo, this.x) - vec3.x());
            float f1 = (float) (Mth.lerp((double) pPartialTicks, this.yo, this.y) - vec3.y());
            float f2 = (float) (Mth.lerp((double) pPartialTicks, this.zo, this.z) - vec3.z());
            Quaternionf quaternion;
            if (this.roll == 0.0F) {
                quaternion = pRenderInfo.rotation();
            } else {
                quaternion = new Quaternionf(pRenderInfo.rotation());
                float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
                quaternion.mul(Axis.ZP.rotation(f3));
            }

            Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
            //vector3f1.transform(quaternion);
            vector3f1.rotate(quaternion);
            Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            float f4 = this.getQuadSize(pPartialTicks);

            for (int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
                //vector3f.transform(quaternion);
                vector3f.rotate(quaternion);
                vector3f.mul(f4);
                vector3f.add(f, f1, f2);
            }

            float f7 = this.getU0();
            float f8 = this.getU1();
            float f5 = this.getV0();
            float f6 = this.getV1();
            int j = this.getLightColor(pPartialTicks);
            buffer.vertex((double) avector3f[0].x(), (double) avector3f[0].y(), (double) avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j);
            buffer.bloomFactor(bloomFactor).endVertex();

            buffer.vertex((double) avector3f[1].x(), (double) avector3f[1].y(), (double) avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j);
            buffer.bloomFactor(bloomFactor).endVertex();

            buffer.vertex((double) avector3f[2].x(), (double) avector3f[2].y(), (double) avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j);
            buffer.bloomFactor(bloomFactor).endVertex();

            buffer.vertex((double) avector3f[3].x(), (double) avector3f[3].y(), (double) avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j);
            buffer.bloomFactor(bloomFactor).endVertex();
        } else {
            super.render(pBuffer, pRenderInfo, pPartialTicks);
        }

    }

    public static class Provider implements ParticleProvider<MadParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        @Nullable
        @Override
        public Particle createParticle(MadParticleOption op, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            int target = op.targetParticle();
            //see ClientboundLevelParticlesPacket
            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.byId(target);
            if (particleType != null) {
                ParticleEngineAccessor particleEngineAccessor = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
                SpriteSet spriteSet = particleEngineAccessor.getSpriteSets().get(ForgeRegistries.PARTICLE_TYPES.getKey(particleType));
                if (spriteSet != null) {
                    return new MadParticle(pLevel, spriteSet, op.spriteFrom(),
                            pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed,
                            op.friction(), op.gravity(), op.collision().get(), op.bounceTime(),
                            op.horizontalRelativeCollisionDiffuse(), op.verticalRelativeCollisionBounce(), op.afterCollisionFriction(), op.afterCollisionGravity(),
                            op.interactWithEntity().get(), op.horizontalInteractFactor(), op.verticalInteractFactor(),
                            op.lifeTime(), ParticleRenderTypesProxy.getType(op.renderType()),
                            op.r(), op.g(), op.b(),
                            op.beginAlpha(), op.endAlpha(), op.alphaMode(),
                            op.beginScale(), op.endScale(), op.scaleMode(),
                            op.child(),
                            op.rollSpeed(),
                            op.xDeflection(), op.xDeflectionAfterCollision(), op.zDeflection(), op.zDeflectionAfterCollision(),
                            op.bloomFactor()
                    );
                }
            }
            return null;
        }
    }
}
