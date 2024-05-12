package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.mixinproxy.ITickType;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(Particle.class)
public class ParticleMixin implements ITickType {

    @Redirect(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
    private RandomSource madparticleWhyMustBeSync() {
        return RandomSource.createNewThreadLocalInstance();
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
        if (TakeOver.ASYNC_TICK_VANILLA_AND_MADPARTICLE.contains(this.getClass())) {
            madparticleTickType = TakeOver.TickType.ASYNC;
        } else if (TakeOver.SYNC_TICK_VANILLA_AND_MADPARTICLE.contains(this.getClass())) {
            madparticleTickType = TakeOver.TickType.SYNC;
        } else {
            madparticleTickType = TakeOver.TickType.UNKNOWN;
        }
    }
}
