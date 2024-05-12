package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")

public enum ParticleRenderTypes implements ITranslatable {
    INSTANCED("gui.mp.de.helper.render_type.instanced"),
    PARTICLE_SHEET_TRANSLUCENT("gui.mp.de.helper.render_type.translucent"),
    TERRAIN_SHEET("gui.mp.de.helper.render_type.terrain"),
    PARTICLE_SHEET_OPAQUE("gui.mp.de.helper.render_type.opaque"),
    PARTICLE_SHEET_LIT("gui.mp.de.helper.render_type.lit"),
    //CUSTOM("gui.mp.de.helper.render_type.vanilla_custom"),
    NO_RENDER("gui.mp.de.helper.render_type.no_render");

    private final String translateKey;

    ParticleRenderTypes(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }
}
