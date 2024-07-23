package cn.ussshenzhou.madparticle.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author USS_Shenzhou
 */
public class MadParticleType extends ParticleType<MadParticleOption> {
    private final MapCodec<MadParticleOption> mapCodec = MapCodec.unit(null);
    private final StreamCodec<? super RegistryFriendlyByteBuf, MadParticleOption> streamCodec = StreamCodec.ofMember(MadParticleOption::writeToNetwork, MadParticleOption::fromNetwork);

    protected MadParticleType() {
        super(true);
    }

    @Override
    public @NotNull MapCodec<MadParticleOption> codec() {
        return mapCodec;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, MadParticleOption> streamCodec() {
        return streamCodec;
    }
}
