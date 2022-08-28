package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.util.MathHelper;
import com.mojang.math.Vector3f;
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
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

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
                       float beginScale, float endScale, ChangeMode scaleMode,
                       MadParticleOption child
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
        this.setSize(0.2f, 0.2f);
        this.scale(beginScale);
        this.scale = beginScale;
        this.endScale = endScale;
        this.scaleMode = scaleMode;
        this.hasPhysics = true;
        this.child = child;
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


    public enum SpriteFrom {
        RANDOM,
        AGE,
        INHERIT;
    }

    private static final double[] sin01 = {0, 0.000148046502605914, 0.000592215314954070, 0.00133259484264708, 0.00236933407524353, 0.00370264508073576, 0.00533280654674539, 0.00726016841886612, 0.00948515770320879, 0.0120082855186116, 0.0148301555047160, 0.0179514737158149, 0.0213730601578324, 0.0250958621579424, 0.0291209697943535, 0.0334496336591418, 0.0380832852815755, 0.0430235606054795, 0.0482723269948982, 0.0538317143415242, 0.0597041509702024, 0.0658924051920205, 0.0723996335469677, 0.0792294370218563, 0.0863859268402791, 0.0938738018218823, 0.101698439828584, 0.109866006498055, 0.118383585369840, 0.127259334722979, 0.136502678090967, 0.146124537684986, 0.156137623117013, 0.166556792295047, 0.177399507826531, 0.188686421772719, 0.200442135882674, 0.212696206427475, 0.225484497543808, 0.238851043759635, 0.252850678352293, 0.267552853332525, 0.283047389835880, 0.299453511720029, 0.316934808940907, 0.335725759051570, 0.356183157019473, 0.378899259661155, 0.405003752527172, 0.437307287109167, 0.500000000000000, 0.562692712890833, 0.594996247472828, 0.621100740338845, 0.643816842980528, 0.664274240948431, 0.683065191059093, 0.700546488279971, 0.716952610164120, 0.732447146667475, 0.747149321647707, 0.761148956240365, 0.774515502456192, 0.787303793572525, 0.799557864117326, 0.811313578227281, 0.822600492173469, 0.833443207704953, 0.843862376882987, 0.853875462315014, 0.863497321909034, 0.872740665277021, 0.881616414630160, 0.890133993501945, 0.898301560171416, 0.906126198178118, 0.913614073159721, 0.920770562978144, 0.927600366453032, 0.934107594807980, 0.940295849029798, 0.946168285658476, 0.951727673005102, 0.956976439394521, 0.961916714718425, 0.966550366340858, 0.970879030205647, 0.974904137842058, 0.978626939842168, 0.982048526284185, 0.985169844495284, 0.987991714481388, 0.990514842296791, 0.992739831581134, 0.994667193453255, 0.996297354919264, 0.997630665924757, 0.998667405157353, 0.999407784685046, 0.999851953497394, 1};

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
            return begin + (end - begin) * getSin(x);
        }),
        INHERIT((begin, end, age, life) -> 0.0f);

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

        public static float getSin(float x) {
            int floor = (int) Math.floor(x * 100);
            return (float) (sin01[floor] + (sin01[floor == 100 ? floor : floor + 1] - sin01[floor]) * (x * 100 - floor) / 100);
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
                            op.friction(), op.gravity(), op.collision().get(), op.bounceTime(),
                            op.horizontalRelativeCollisionDiffuse(), op.verticalRelativeCollisionBounce(), op.afterCollisionFriction(), op.afterCollisionGravity(),
                            op.interactWithEntity().get(), op.horizontalInteractFactor(), op.verticalInteractFactor(),
                            op.lifeTime(), op.renderType().getType(),
                            op.r(), op.g(), op.b(),
                            op.beginAlpha(), op.endAlpha(), op.alphaMode(),
                            op.beginScale(), op.endScale(), op.scaleMode(),
                            op.child()
                    );
                }
            }
            return null;
        }
    }
}
