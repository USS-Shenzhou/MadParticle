package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

public class TakeOverTypeUtilC {

    public static SingleQuadParticle.Layer getLayer(TakeOverType t) {
        return switch (t) {
            case INSTANCED, DEFAULT -> SingleQuadParticle.Layer.TRANSLUCENT;
            case INSTANCED_TERRAIN -> SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
        };
    }

    public static ParticleRenderType getParticleRenderType(TakeOverType t) {
        return switch (t) {
            case INSTANCED -> ModParticleRenderTypes.INSTANCED;
            case INSTANCED_TERRAIN -> ModParticleRenderTypes.INSTANCED_TERRAIN;
            case DEFAULT -> ParticleRenderType.SINGLE_QUADS;
        };
    }
}
