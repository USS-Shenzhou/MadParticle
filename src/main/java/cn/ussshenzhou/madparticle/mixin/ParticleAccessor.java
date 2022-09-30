package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor
    float getFriction();

    @Accessor
    float getGravity();

    @Accessor
    float getRoll();

    @Accessor
    float getAlpha();

    @Accessor
    float getBbWidth();

    @Accessor
    float getBbHeight();

    @Accessor
    float getRCol();

    @Accessor
    float getGCol();

    @Accessor
    float getBCol();
}
