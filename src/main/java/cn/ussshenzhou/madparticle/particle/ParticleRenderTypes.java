package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.designer.universal.util.ToTranslatableString;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")

public enum ParticleRenderTypes implements ToTranslatableString {
    PARTICLE_SHEET_TRANSLUCENT("gui.mp.de.helper.render_type.translucent"),
    TERRAIN_SHEET("gui.mp.de.helper.render_type.terrain"),
    PARTICLE_SHEET_OPAQUE("gui.mp.de.helper.render_type.opaque"),
    PARTICLE_SHEET_LIT("gui.mp.de.helper.render_type.lit"),
    CUSTOM("gui.mp.de.helper.render_type.custom"),
    NO_RENDER("gui.mp.de.helper.render_type.no_render");

    private final String translateKey;

    ParticleRenderTypes(String translateKey) {
        this.translateKey = translateKey;
    }

    @SuppressWarnings("AlibabaSwitchStatement")
    @OnlyIn(Dist.CLIENT)
    public static ParticleRenderType getType(ParticleRenderTypes enumType) {
        switch (enumType) {
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

    @Override
    public String toTranslateKey() {
        return translateKey;
    }
}
