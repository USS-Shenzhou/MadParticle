package cn.ussshenzhou.madparticle.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")

public enum ParticleRenderTypes {
    TERRAIN_SHEET,
    PARTICLE_SHEET_OPAQUE,
    PARTICLE_SHEET_LIT,
    PARTICLE_SHEET_TRANSLUCENT,
    CUSTOM,
    NO_RENDER;


    @SuppressWarnings("AlibabaSwitchStatement")
    @OnlyIn(Dist.CLIENT)
    public static ParticleRenderType getType(ParticleRenderTypes enumType) {
        switch (enumType){
            case TERRAIN_SHEET -> {
                return ParticleRenderType.TERRAIN_SHEET;
            }
            case PARTICLE_SHEET_OPAQUE -> {
                return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            }
            case PARTICLE_SHEET_LIT -> {
                return ParticleRenderType.PARTICLE_SHEET_LIT;
            }
            case PARTICLE_SHEET_TRANSLUCENT -> {
                return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            }
            case CUSTOM -> {
                return ParticleRenderType.CUSTOM;
            }
            default -> {
                return ParticleRenderType.NO_RENDER;
            }
        }
    }
}
