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

    @SuppressWarnings("AlibabaSwitchStatement")
    public static ParticleRenderType getType(ParticleRenderTypes enumType) {
        switch (enumType) {
            case TERRAIN_SHEET -> {
                return isIrisOn() ? ParticleRenderType.TERRAIN_SHEET : MadParticleRenderTypes.TERRAIN_SHEET;
            }
            case PARTICLE_SHEET_OPAQUE -> {
                return isIrisOn() ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : MadParticleRenderTypes.PARTICLE_SHEET_OPAQUE;
            }
            case PARTICLE_SHEET_LIT -> {
                return isIrisOn() ? ParticleRenderType.PARTICLE_SHEET_LIT : MadParticleRenderTypes.PARTICLE_SHEET_LIT;
            }
            case PARTICLE_SHEET_TRANSLUCENT -> {
                return isIrisOn() ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : MadParticleRenderTypes.PARTICLE_SHEET_TRANSLUCENT;
            }
            case CUSTOM -> {
                return ParticleRenderType.CUSTOM;
            }
            default -> {
                return ParticleRenderType.NO_RENDER;
            }
        }
    }

    private static boolean isIrisOn() {
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstance = irisApi.getMethod("getInstance");
            getInstance.setAccessible(true);
            Method isShaderPackInUse = irisApi.getMethod("isShaderPackInUse");
            isShaderPackInUse.setAccessible(true);
            return (boolean) isShaderPackInUse.invoke(getInstance.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException ignored) {
            return false;
        }
    }

    @Override
    public String toTranslateKey() {
        return translateKey;
    }
}
