package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import cn.ussshenzhou.t88.gui.util.ITranslatable;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

import java.util.function.Function;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")

public enum TakeOverType implements ITranslatable {
    INSTANCED("gui.mp.de.helper.render_type.instanced"),
    INSTANCED_TERRAIN("gui.mp.de.helper.render_type.terrain"),
    DEFAULT("gui.mp.de.helper.render_type.default");

    private final String translateKey;

    TakeOverType(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }
}
