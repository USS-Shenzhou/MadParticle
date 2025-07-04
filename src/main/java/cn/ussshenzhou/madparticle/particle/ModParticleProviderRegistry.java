package cn.ussshenzhou.madparticle.particle;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ModParticleProviderRegistry {
    @SubscribeEvent
    public static void onParticleProviderRegistry(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticleTypeRegistry.MAD_PARTICLE.get(), new MadParticle.Provider());
    }
}
