package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


/**
 * @author USS_Shenzhou
 */
public class ModParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MadParticle.MOD_ID);

    public static final Supplier<ParticleType<MadParticleOption>> MAD_PARTICLE = PARTICLE_TYPES.register("mad_particle", MadParticleType::new);
}
