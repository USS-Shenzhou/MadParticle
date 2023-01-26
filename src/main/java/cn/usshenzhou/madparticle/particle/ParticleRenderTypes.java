package cn.usshenzhou.madparticle.particle;

import cn.usshenzhou.madparticle.command.ToTranslatableString;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.ParticleRenderType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    @Override
    public String toTranslateKey() {
        return translateKey;
    }
}
