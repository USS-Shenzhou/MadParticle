package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.util.MovementHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@Mixin(Particle.class)
public class ParticleMixin implements ITickType {

    @Redirect(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
    private RandomSource madparticleWhyMustBeSync() {
        return RandomSource.createThreadLocalInstance();
    }


    @Unique
    private TakeOver.TickType madparticleTickType;

    @Override
    public TakeOver.TickType getTickType() {
        return madparticleTickType;
    }

    @Override
    public void setTickType(TakeOver.TickType tickType) {
        madparticleTickType = tickType;
    }

    @Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V", at = @At("TAIL"))
    private void madparticleSetMadparticleTickType(ClientLevel pLevel, double pX, double pY, double pZ, CallbackInfo ci) {
        if (TakeOver.ASYNC_TICK.contains(this.getClass())) {
            madparticleTickType = TakeOver.TickType.ASYNC;
        } else if (TakeOver.SYNC_TICK.contains(this.getClass())) {
            madparticleTickType = TakeOver.TickType.SYNC;
        } else {
            madparticleTickType = TakeOver.TickType.UNKNOWN;
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 madparticleUseFasterCollision(CollisionContext source, Vec3 vec3, AABB boundingBox, Level level, List<VoxelShape> entityColliders) {
        var result = MovementHelper.collideBoundingBox(vec3.x, vec3.y, vec3.z, boundingBox, level);
        return result == null ? vec3 : result;
    }
}
