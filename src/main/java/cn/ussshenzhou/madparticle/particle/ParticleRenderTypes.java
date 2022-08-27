package cn.ussshenzhou.madparticle.particle;

import net.minecraft.client.particle.ParticleRenderType;

import java.util.ArrayList;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ParticleRenderTypes {
    TERRAIN_SHEET(ParticleRenderType.TERRAIN_SHEET),
    PARTICLE_SHEET_OPAQUE(ParticleRenderType.PARTICLE_SHEET_OPAQUE),
    PARTICLE_SHEET_LIT(ParticleRenderType.PARTICLE_SHEET_LIT),
    PARTICLE_SHEET_TRANSLUCENT(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT),
    CUSTOM(ParticleRenderType.CUSTOM),
    NO_RENDER(ParticleRenderType.NO_RENDER);

    private final ParticleRenderType type;

    private ParticleRenderTypes(ParticleRenderType type){
        this.type = type;
    }

    public ParticleRenderType getType() {
        return type;
    }
}
