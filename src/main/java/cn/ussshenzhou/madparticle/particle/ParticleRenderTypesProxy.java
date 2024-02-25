package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

/**
 * @author USS_Shenzhou
 */
public class ParticleRenderTypesProxy {

    @SuppressWarnings("AlibabaSwitchStatement")
    public static ParticleRenderType getType(ParticleRenderTypes enumType) {
        return switch (enumType) {
            case INSTANCED -> ModParticleRenderTypes.INSTANCED;
            case TERRAIN_SHEET ->
                    MadParticle.irisOn ? ParticleRenderType.TERRAIN_SHEET : ModParticleRenderTypes.Traditional.TERRAIN_SHEET;
            case PARTICLE_SHEET_OPAQUE ->
                    MadParticle.irisOn ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : ModParticleRenderTypes.Traditional.PARTICLE_SHEET_OPAQUE;
            case PARTICLE_SHEET_LIT ->
                    MadParticle.irisOn ? ParticleRenderType.PARTICLE_SHEET_LIT : ModParticleRenderTypes.Traditional.PARTICLE_SHEET_LIT;
            case PARTICLE_SHEET_TRANSLUCENT ->
                    MadParticle.irisOn ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ModParticleRenderTypes.Traditional.PARTICLE_SHEET_TRANSLUCENT;
            /*case CUSTOM -> {
                return ParticleRenderType.CUSTOM;
            }*/
            default -> ParticleRenderType.NO_RENDER;
        };
    }
}
