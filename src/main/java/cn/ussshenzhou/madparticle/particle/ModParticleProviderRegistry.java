package cn.ussshenzhou.madparticle.particle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticleProviderRegistry {
    @SubscribeEvent
    public static void onParticleProviderRegistry(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticleTypeRegistry.MAD_PARTICLE.get(), new MadParticle.Provider());
    }
}
