package cn.ussshenzhou.madparticle.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ParticleRenderTypesProxy {
    private static boolean irisOn;

    @SuppressWarnings("AlibabaSwitchStatement")
    public static ParticleRenderType getType(ParticleRenderTypes enumType) {
        switch (enumType) {
            case TERRAIN_SHEET -> {
                return irisOn ? ParticleRenderType.TERRAIN_SHEET : MadParticleRenderTypes.TERRAIN_SHEET;
            }
            case PARTICLE_SHEET_OPAQUE -> {
                return irisOn ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : MadParticleRenderTypes.PARTICLE_SHEET_OPAQUE;
            }
            case PARTICLE_SHEET_LIT -> {
                return irisOn ? ParticleRenderType.PARTICLE_SHEET_LIT : MadParticleRenderTypes.PARTICLE_SHEET_LIT;
            }
            case PARTICLE_SHEET_TRANSLUCENT -> {
                return irisOn ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : MadParticleRenderTypes.PARTICLE_SHEET_TRANSLUCENT;
            }
            /*case CUSTOM -> {
                return ParticleRenderType.CUSTOM;
            }*/
            default -> {
                return ParticleRenderType.NO_RENDER;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            irisOn = checkIrisOn();
        }
    }

    private static boolean checkIrisOn() {
        //return true;
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
}
