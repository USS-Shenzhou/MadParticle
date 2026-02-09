package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    @Invoker
    <T extends ParticleOptions> Particle callMakeParticle(T pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed);

    @Accessor
    Queue<Particle> getParticlesToAdd();
}
