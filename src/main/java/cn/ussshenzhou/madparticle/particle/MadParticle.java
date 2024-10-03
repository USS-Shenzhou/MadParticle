package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.util.AABBHelper;
import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.madparticle.util.MovementHelper;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static cn.ussshenzhou.madparticle.util.MetaKeys.*;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public class MadParticle extends TextureSheetParticle {
    protected final SpriteSet sprites;
    protected final SpriteFrom spriteFrom;
    protected final float beginGravity;
    protected boolean collision;
    protected final int bounceTime;
    protected final float horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce;
    protected final float afterCollisionFriction;
    protected final float afterCollisionGravity;
    protected final boolean interactWithEntity;
    protected final float horizontalInteractFactor, verticalInteractFactor;
    protected final ParticleRenderType particleRenderType;
    protected final float beginAlpha, endAlpha;
    protected final ChangeMode alphaMode;
    protected final float beginScale, endScale;
    protected final ChangeMode scaleMode;
    protected final MadParticleOption child;
    protected float rollSpeed;
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

    private final CompoundTag meta;
    private float[] dxComplex = null;
    private float[] dyComplex = null;
    private float[] dzComplex = null;
    private int disappearOnCollision = 0;
    public TimeMode timeMode = TimeMode.NORMAL;
    private float[] xTrack = null;
    private float[] yTrack = null;
    private float[] zTrack = null;
    private float[] alphaTrack = null;
    private float[] scaleTrack = null;
    private int[] light = null;

    @SuppressWarnings("AlibabaSwitchStatement")
    public MadParticle(ClientLevel pLevel, SpriteSet spriteSet, SpriteFrom spriteFrom,
                       double pX, double pY, double pZ, double vx, double vy, double vz,
                       float friction, float gravity, boolean collision, int bounceTime,
                       float horizontalRelativeCollisionDiffuse, float verticalRelativeCollisionBounce, float afterCollisionFriction, float afterCollisionGravity,
                       boolean interactWithEntity, float horizontalInteractFactor, float verticalInteractFactor,
                       int lifeTime, ParticleRenderType renderType,
                       float r, float g, float b,
                       float beginAlpha, float endAlpha, ChangeMode alphaMode,
                       float beginScale, float endScale, ChangeMode scaleMode,
                       MadParticleOption child,
                       float rollSpeed,
                       float xDeflection, float xDeflectionAfterCollision, float zDeflection, float zDeflectionAfterCollision,
                       float bloomFactor,
                       CompoundTag meta
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
        ((ITickType) this).setTickType(interactWithEntity ? TakeOver.TickType.SYNC : TakeOver.TickType.ASYNC);
        this.horizontalInteractFactor = horizontalInteractFactor;
        this.verticalInteractFactor = verticalInteractFactor;
        this.lifetime = lifeTime;
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
        this.rollSpeed = (float) (rollSpeed * (1 + 0.1 * MathHelper.signedRandom(random)));
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
        this.meta = meta;
        //keep it at last
        initMetaTags();
    }

    private void initMetaTags() {
        handleLifeTime();
        handleDxyz();
        if (meta.contains(DISAPPEAR_ON_COLLISION.get())) {
            disappearOnCollision = meta.getInt(DISAPPEAR_ON_COLLISION.get());
        }
        handleLight();
        handlePreCalculate();
        //keep it at last
        handleTenet();
    }

    private void handleLifeTime() {
        float maxError = 0.1f;
        if (meta.contains(LIFE_ERROR.get())) {
            maxError = meta.getInt(LIFE_ERROR.get()) / 100f;
        }
        lifetime *= (1 + maxError * MathHelper.signedRandom(random));
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void handleDxyz() {
        int length = Math.min(lifetime, 100);
        if (meta.contains(DX.get())) {
            Expression e = new ExpressionBuilder(meta.getString(DX.get()))
                    .variable("t")
                    .build();
            dxComplex = new float[length + 1];
            for (int i = 0; i <= length; i++) {
                e.setVariable("t", (double) i / length);
                dxComplex[i] = (float) e.evaluate();
            }
        }
        if (meta.contains(DY.get())) {
            Expression e = new ExpressionBuilder(meta.getString(DY.get()))
                    .variable("t")
                    .build();
            dyComplex = new float[length + 1];
            for (int i = 0; i <= length; i++) {
                e.setVariable("t", (double) i / length);
                dyComplex[i] = (float) e.evaluate();
            }
        }
        if (meta.contains(DZ.get())) {
            Expression e = new ExpressionBuilder(meta.getString(DZ.get()))
                    .variable("t")
                    .build();
            dzComplex = new float[length + 1];
            for (int i = 0; i <= length; i++) {
                e.setVariable("t", (double) i / length);
                dzComplex[i] = (float) e.evaluate();
            }
        }
    }

    private void handlePreCalculate() {
        if (!meta.getBoolean(PRE_CAL.get()) || meta.getBoolean(TENET.get())) {
            return;
        }
        initRecordArrays();
        var pos = this.getPos();
        while (true) {
            tick();
            if (removed) {
                break;
            } else {
                record(age - 1);
            }
        }
        //clean and get ready
        timeMode = TimeMode.PRE_CAL;
        removed = false;
        this.age = 0;
        this.setPos(pos.x, pos.y, pos.z);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.scale(1 / endScale * beginScale);
        this.scale = beginScale;
        this.alpha = beginAlpha;
    }

    private void initRecordArrays() {
        xTrack = new float[lifetime];
        yTrack = new float[lifetime];
        zTrack = new float[lifetime];
        if (endAlpha != beginAlpha) {
            alphaTrack = new float[lifetime];
        }
        if (endScale != beginScale) {
            scaleTrack = new float[lifetime];
        }
    }

    private void record(int i) {
        xTrack[i] = (float) x;
        yTrack[i] = (float) y;
        zTrack[i] = (float) z;
        if (endAlpha != beginAlpha) {
            alphaTrack[i] = alpha;
        }
        if (endScale != beginScale) {
            scaleTrack[i] = scale;
        }
    }

    private void handleTenet() {
        if (!meta.getBoolean(TENET.get())) {
            return;
        }
        initRecordArrays();
        while (true) {
            tick();
            if (removed) {
                break;
            } else {
                record(lifetime - age - 1);
            }
        }
        timeMode = TimeMode.REVERSE;
        removed = false;
        this.age = 0;
    }

    private void handleLight() {
        int length = Math.min(lifetime, 100);
        if (!meta.contains(LIGHT.get())) {
            return;
        }
        Expression e = new ExpressionBuilder(meta.getString(LIGHT.get()))
                .variable("t")
                .build();
        light = new int[length + 1];
        for (int i = 0; i <= length; i++) {
            e.setVariable("t", (double) i / length);
            light[i] = Mth.clamp((int) e.evaluate(), 0, 15);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        switch (timeMode) {
            case NORMAL -> normalTick();
            case PRE_CAL -> preCalculatedTick();
            case REVERSE -> reversedTick();
        }
        sharedTick();
        age++;
        if (this.age >= this.lifetime) {
            this.remove();
        }
    }

    private void tickAlphaAndSize() {
        //alpha
        this.alpha = alphaMode.lerp(beginAlpha, endAlpha, age, lifetime);
        //size
        if (endScale != beginScale) {
            float newScale = scaleMode.lerp(beginScale, endScale, age, lifetime);
            this.scale(1 / scale * newScale);
            scale = newScale;
        }
    }

    @Override
    protected void setSize(float pWidth, float pHeight) {
        if (pWidth != this.bbWidth || pHeight != this.bbHeight) {
            this.bbWidth = pWidth;
            this.bbHeight = pHeight;
            AABB aabb = this.getBoundingBox();
            double d0 = (aabb.minX + aabb.maxX - (double) pWidth) / 2.0D;
            double d1 = (aabb.minZ + aabb.maxZ - (double) pWidth) / 2.0D;
            this.setBoundingBox(AABBHelper.set(getBoundingBox(), d0, aabb.minY, d1, d0 + (double) this.bbWidth, aabb.minY + (double) this.bbHeight, d1 + (double) this.bbWidth));
        }
    }

    private void normalTick() {
        tickAlphaAndSize();
        //interact with Entity
        if (interactWithEntity) {
            LivingEntity entity = level.getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat().range(2), null, x, y, z, this.getBoundingBox().inflate(0.4));
            if (entity != null) {
                Vec3 v = entity.getDeltaMovement();
                this.xd += v.x * random.nextFloat() * horizontalInteractFactor;
                double y0;
                if (entity.onGround()) {
                    y0 = Math.sqrt(v.x * v.x + v.z * v.z);
                } else {
                    y0 = v.y;
                }
                y0 *= verticalInteractFactor;
                if (y0 > 0) {
                    this.onGround = false;
                }
                this.yd += (!entity.onGround() && v.y < 0 ? -y0 : y0);
                this.zd += v.z * random.nextFloat() * horizontalInteractFactor;
                this.gravity = beginGravity;
                this.friction = frictionInitial;
                this.xDeflection = xDeflectionInitial;
                this.zDeflection = zDeflectionInitial;
            }
        }
        //dx dy dz, gravity and deflection, and friction
        if (this.dyComplex != null) {
            this.yd = MathHelper.getFromT((float) age / lifetime, dyComplex);
        } else {
            this.yd -= 0.04 * (double) this.gravity;
            this.yd *= this.friction;
        }
        if (this.dxComplex != null) {
            this.xd = MathHelper.getFromT((float) age / lifetime, dxComplex);
        } else {
            this.xd += 0.04 * this.xDeflection;
            this.xd *= this.friction;
        }
        if (this.dzComplex != null) {
            this.zd = MathHelper.getFromT((float) age / lifetime, dzComplex);
        } else {
            this.zd += 0.04 * this.zDeflection;
            this.zd *= this.friction;
        }
        //done
        if (this.xd != 0 || this.yd != 0 || this.zd != 0) {
            this.move(this.xd, this.yd, this.zd);
        }
    }

    private void preCalculatedTick() {
        tickAlphaAndSize();
        //use setPos instead of move to avoid unexpected changes and special handles of dx/y/z.
        setPos(xTrack[age], yTrack[age], zTrack[age]);
    }

    private void reversedTick() {
        if (endAlpha != beginAlpha) {
            this.alpha = alphaTrack[age];
        }
        if (endScale != beginScale) {
            float newScale = scaleTrack[age];
            this.scale(1 / scale * newScale);
            scale = newScale;
        }
        //no interact with Entity
        setPos(xTrack[age], yTrack[age], zTrack[age]);
    }

    private void sharedTick() {
        //sprite
        if (this.spriteFrom == SpriteFrom.AGE) {
            if (!isReversed()) {
                setSpriteFromAge(sprites);
            } else {
                setSpriteFromAgeReversed(sprites);
            }
        }
        //roll
        if (rollSpeed != 0) {
            this.oRoll = this.roll;
            if (!this.onGround) {
                if (!isReversed()) {
                    this.roll += (float) Math.PI * rollSpeed * 2.0F;
                } else {
                    this.roll -= (float) Math.PI * rollSpeed * 2.0F;
                }
            }
        }
    }

    public void setSpriteFromAgeReversed(SpriteSet pSprite) {
        if (!this.removed) {
            this.setSprite(pSprite.get(lifetime - age - 1, this.lifetime));
        }
    }

    @SuppressWarnings("AlibabaAvoidDoubleOrFloatEqualCompare")
    @Override
    public void move(double xDelta, double yDelta, double zDelta) {
        double xDeltaCollided = xDelta;
        double yDeltaCollided = yDelta;
        double zDeltaCollided = zDelta;
        double r2 = xDelta * xDelta + yDelta * yDelta + zDelta * zDelta;
        boolean c = collision && isNormalTime() && this.hasPhysics && (xDelta != 0.0D || yDelta != 0.0D || zDelta != 0.0D) && r2 < MAXIMUM_COLLISION_VELOCITY_SQUARED;
        if (c) {
            Vec3 vec3 = MovementHelper.collideBoundingBox(null, xDelta, yDelta, zDelta, this.getBoundingBox(), this.level, List.of());
            //Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(xDelta, yDelta, zDelta), this.getBoundingBox(), this.level, List.of());
            xDeltaCollided = vec3 == null ? xDelta : vec3.x;
            yDeltaCollided = vec3 == null ? yDelta : vec3.y;
            zDeltaCollided = vec3 == null ? zDelta : vec3.z;
        }
        if (xDelta != 0.0D || yDelta != 0.0D || zDelta != 0.0D) {
            this.setBoundingBox(AABBHelper.move(this.getBoundingBox(), xDeltaCollided, yDeltaCollided, zDeltaCollided));
            this.setLocationFromBoundingbox();
        }
        if (c) {
            //hit XOZ
            if (yDeltaCollided != yDelta) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, xd, zd);
                    this.xd = v.x;
                    this.yd = -yDeltaCollided * (random.nextDouble() * verticalRelativeCollisionBounce);
                    this.zd = v.y;
                    updateAfterCollision();
                } else {
                    this.gravity = 0;
                    this.onGround = true;
                }
                this.friction = afterCollisionFriction;
                return;
            }
            //hit YOZ
            if (xDeltaCollided != xDelta) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, yd, zd);
                    this.xd = -xDeltaCollided * (random.nextDouble() * verticalRelativeCollisionBounce);
                    this.yd = v.x;
                    this.zd = v.y;
                    updateAfterCollision();
                }
                this.friction = afterCollisionFriction;
                return;
            }
            //hit XOY
            if (zDeltaCollided != zDelta) {
                if (bounceCount < bounceTime) {
                    Vec2 v = horizontalRelativeCollision(r2, xd, yd);
                    this.xd = v.x;
                    this.yd = v.y;
                    this.zd = -zDeltaCollided * (random.nextDouble() * verticalRelativeCollisionBounce);
                    updateAfterCollision();
                }
                this.friction = afterCollisionFriction;
                return;
            }
        }
    }

    private void updateAfterCollision() {
        bounceCount++;
        this.gravity = afterCollisionGravity;
        this.xDeflection = xDeflectionAfterCollision;
        this.zDeflection = zDeflectionAfterCollision;
        this.dxComplex = null;
        this.dyComplex = null;
        this.dzComplex = null;
        if (disappearOnCollision > 0 && bounceCount >= disappearOnCollision) {
            this.remove();
        }
    }

    public Vec2 horizontalRelativeCollision(double r2, double d1, double d2) {
        if (horizontalRelativeCollisionDiffuse == 0) {
            return new Vec2(0, 0);
        }
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
            AddParticleHelper.addParticleClientAsync2Async(child.inheritOrContinue(this));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return particleRenderType;
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public Vector3f getPosition(float partialTicks) {
        return new Vector3f((float) Mth.lerp(partialTicks, xo, x), (float) Mth.lerp(partialTicks, yo, y), (float) Mth.lerp(partialTicks, zo, z));
    }

    public float getX(float partialTicks) {
        return (float) Mth.lerp(partialTicks, xo, x);
    }

    public float getY(float partialTicks) {
        return (float) Mth.lerp(partialTicks, yo, y);
    }

    public float getZ(float partialTicks) {
        return (float) Mth.lerp(partialTicks, zo, z);
    }

    public float getRoll(float partialTicks) {
        return Mth.lerp(partialTicks, oRoll, roll);
    }

    public Vec3 getSpeed() {
        return new Vec3(xd, yd, zd);
    }

    public Vector3f getColor() {
        return new Vector3f(rCol, gCol, bCol);
    }

    public float getR() {
        return rCol;
    }

    public float getG() {
        return gCol;
    }

    public float getB() {
        return bCol;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getBloomFactor() {
        return bloomFactor;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        if (particleRenderType == ModParticleRenderTypes.INSTANCED) {
            throw new UnsupportedOperationException();
        } else {
            super.render(pBuffer, pRenderInfo, pPartialTicks);
        }

    }

    @Override
    public int getLightColor(float pPartialTick) {
        return checkEmit(super.getLightColor(pPartialTick));
    }

    public int checkEmit(int levelLight) {
        if (light == null) {
            return levelLight;
        } else {
            return levelLight & 0b1111_0000_00000000_0000_0000 | ((int) MathHelper.getFromT((float) age / lifetime, light) << 4);
        }
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    private boolean isReversed() {
        return timeMode == TimeMode.REVERSE;
    }

    private boolean isPreCalculate() {
        return timeMode == TimeMode.PRE_CAL;
    }

    private boolean isNormalTime() {
        return timeMode == TimeMode.NORMAL;
    }

    public enum TimeMode {
        NORMAL,
        PRE_CAL,
        REVERSE
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public static class Provider implements ParticleProvider<MadParticleOption> {

        public Provider() {
        }

        @Nullable
        @Override
        public Particle createParticle(MadParticleOption op, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            int target = op.targetParticle();
            //see ClientboundLevelParticlesPacket
            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.byId(target);
            if (particleType != null) {
                ParticleEngineAccessor particleEngineAccessor = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
                SpriteSet spriteSet = particleEngineAccessor.getSpriteSets().get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType));
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
                            op.bloomFactor(),
                            op.meta()
                    );
                }
            }
            return null;
        }
    }
}
