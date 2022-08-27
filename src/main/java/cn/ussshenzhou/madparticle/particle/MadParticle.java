package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public class MadParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final SpriteFrom spriteFrom;
    private final float beginGravity;
    private final boolean collision;
    private final int bounceTime;
    private final double horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce;
    private final float afterCollisionFriction;
    private final float afterCollisionGravity;
    private final boolean interactWithEntity;
    private final double horizontalInteractFactor, verticalInteractFactor;
    private final ParticleRenderType particleRenderType;
    private final float beginAlpha, endAlpha;
    private final ChangeMode alphaMode;
    private final float beginScale, endScale;
    private final ChangeMode scaleMode;

    private int bounceCount = 0;
    private float scale;
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);
    private static final float MAX_DIRECTIONAL_LOSS = 0.65f;
    private static final int BASE = 10;

    @SuppressWarnings("AlibabaSwitchStatement")
    public MadParticle(ClientLevel pLevel, SpriteSet spriteSet, SpriteFrom spriteFrom,
                       double pX, double pY, double pZ, double vx, double vy, double vz,
                       float friction, float gravity, boolean collision, int bounceTime,
                       double horizontalRelativeCollisionDiffuse, double verticalRelativeCollisionBounce, float afterCollisionFriction, float afterCollisionGravity,
                       boolean interactWithEntity, double horizontalInteractFactor, double verticalInteractFactor,
                       int lifeTime, ParticleRenderType renderType,
                       float r, float g, float b,
                       float beginAlpha, float endAlpha, ChangeMode alphaMode,
                       float beginScale, float endScale, ChangeMode scaleMode
    ) {
        super(pLevel, pX, pY, pZ);
        this.sprites = spriteSet;
        this.spriteFrom = spriteFrom;
        switch (spriteFrom) {
            case AGE -> this.setSpriteFromAge(spriteSet);
            case RANDOM -> this.pickSprite(spriteSet);
        }
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = friction;
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
        this.endAlpha = endAlpha;
        this.alphaMode = alphaMode;
        this.beginScale = beginScale;
        this.scale(beginScale);
        this.scale = beginScale;
        this.endScale = endScale;
        this.scaleMode = scaleMode;
        this.hasPhysics = true;
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
                    this.yd += Math.max(v.y * verticalInteractFactor, Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.z, 2)) * verticalInteractFactor);
                    this.zd += v.z * random.nextFloat() * horizontalInteractFactor;
                    this.gravity = beginGravity;
                }
            }
            //normal
            this.yd -= 0.04D * (double) this.gravity;
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
        }
    }

    @SuppressWarnings("AlibabaAvoidDoubleOrFloatEqualCompare")
    @Override
    public void move(double pX, double pY, double pZ) {
        double x0 = pX;
        double y0 = pY;
        double z0 = pZ;
        double r2 = pX * pX + pY * pY + pZ * pZ;
        if (this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D) && r2 < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
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
                    this.gravity = afterCollisionGravity;
                    bounceCount++;
                } else {
                    this.gravity = 0;
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
                    this.gravity = afterCollisionGravity;
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
                    this.gravity = afterCollisionGravity;
                    bounceCount++;
                }
                this.friction = afterCollisionFriction;
                return;
            }
        }
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
    public ParticleRenderType getRenderType() {
        return particleRenderType;
    }


    public enum SpriteFrom {
        RANDOM,
        AGE;
    }

    public enum ChangeMode {
        LINEAR((begin, end, age, life) -> {
            float x = age / (float) life;
            return begin + (end - begin) * x;
        }),
        INDEX((begin, end, age, life) -> {
            float x = age / (float) life;
            return begin + (float) ((end - begin) * (Math.pow(BASE, x) - 1) / (BASE - 1));
        }),
        SIN((begin, end, age, life) -> {
            float x = age / (float) life;
            return begin + (end - begin) * (1 + (float) Math.sin(x * Math.PI - Math.PI / 2)) * 0.5f;
        });

        @FunctionalInterface
        interface LerpFunction<A, B, C, D, R> {
            @SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
            public R apply(A begin, B end, C age, D lifeTime);
        }

        private final LerpFunction<Float, Float, Integer, Integer, Float> lerp;

        ChangeMode(LerpFunction<Float, Float, Integer, Integer, Float> lerpFunction) {
            this.lerp = lerpFunction;
        }

        public float lerp(float begin, float end, int age, int life) {
            return this.lerp.apply(begin, end, age, life);
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
            ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(target);
            if (particleType != null) {
                ParticleEngineAccessor particleEngineAccessor = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
                SpriteSet spriteSet = particleEngineAccessor.getSpriteSets().get(particleType.getRegistryName());
                if (spriteSet != null) {
                    return new MadParticle(pLevel, spriteSet, op.spriteFrom(),
                            pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed,
                            op.friction(), op.gravity(), op.collision(), op.bounceTime(),
                            op.horizontalRelativeCollisionDiffuse(), op.verticalRelativeCollisionBounce(), op.afterCollisionFriction(), op.afterCollisionGravity(),
                            op.interactWithEntity(), op.horizontalInteractFactor(), op.verticalInteractFactor(),
                            op.lifeTime(), op.renderType().getType(),
                            op.r(), op.g(), op.b(),
                            op.beginAlpha(), op.endAlpha(), op.alphaMode(),
                            op.beginScale(), op.endScale(), op.scaleMode()
                    );
                }
            }
            return null;
        }
    }
}
