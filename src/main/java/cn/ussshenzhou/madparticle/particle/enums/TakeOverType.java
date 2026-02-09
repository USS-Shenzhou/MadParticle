package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.t88.gui.util.ITranslatable;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

import javax.annotation.Nullable;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")

public enum TakeOverType implements ITranslatable {
    INSTANCED("gui.mp.de.helper.render_type.instanced", SingleQuadParticle.Layer.TRANSLUCENT, ModParticleRenderTypes.INSTANCED),
    INSTANCED_TERRAIN("gui.mp.de.helper.render_type.terrain", SingleQuadParticle.Layer.TERRAIN, ModParticleRenderTypes.INSTANCED_TERRAIN),
    DEFAULT("gui.mp.de.helper.render_type.default", SingleQuadParticle.Layer.TRANSLUCENT, ParticleRenderType.SINGLE_QUADS);

    private final String translateKey;
    private final SingleQuadParticle.Layer layer;
    private final ParticleRenderType particleRenderType;

    TakeOverType(String translateKey, SingleQuadParticle.Layer layer, ParticleRenderType particleRenderType) {
        this.translateKey = translateKey;
        this.layer = layer;
        this.particleRenderType = particleRenderType;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }

    public SingleQuadParticle.Layer getLayer() {
        return layer;
    }

    public ParticleRenderType getParticleRenderType() {
        return particleRenderType;
    }
}
