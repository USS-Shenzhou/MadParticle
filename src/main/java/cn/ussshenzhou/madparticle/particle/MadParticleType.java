package cn.ussshenzhou.madparticle.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
public class MadParticleType extends ParticleType<MadParticleOption> {

    protected MadParticleType() {
        super(true);
    }

    @Override
    public @NotNull MapCodec<MadParticleOption> codec() {
        return MadParticleOption.MAP_CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, MadParticleOption> streamCodec() {
        return MadParticleOption.STREAM_CODEC;
    }
}
