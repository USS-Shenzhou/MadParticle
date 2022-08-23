package cn.ussshenzhou.madparticle.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/**
 * @author USS_Shenzhou
 */
public class MadParticleType extends ParticleType<MadParticleOption> {
    public MadParticleType() {
        super(false,MadParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<MadParticleOption> codec() {
        return null;
    }
}
