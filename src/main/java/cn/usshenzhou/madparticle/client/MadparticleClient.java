package cn.usshenzhou.madparticle.client;

import cn.usshenzhou.madparticle.Madparticle;
import cn.usshenzhou.madparticle.network.MadParticlePacket;
import cn.usshenzhou.madparticle.particle.MadParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * @author USS_Shenzhou
 */
@Environment(EnvType.CLIENT)
public class MadparticleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(((atlasTexture, registry) -> {
            registry.register(new ResourceLocation("madparticle","particle/mad_particle"));
        }));

        ParticleFactoryRegistry.getInstance().register(Madparticle.MAD_PARTICLE, MadParticle.Provider::new);

        ClientPlayNetworking.registerGlobalReceiver(MadParticlePacket.CHANNEL_NAME,(client, handler, buf, responseSender) -> {
            MadParticlePacket packet = new MadParticlePacket(buf);
            client.execute(packet::clientHandler);
        });
    }
}
