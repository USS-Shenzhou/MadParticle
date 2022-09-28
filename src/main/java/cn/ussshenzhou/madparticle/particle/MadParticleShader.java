package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author zomb-676
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = MadParticle.MOD_ID)
public class MadParticleShader {

    public static ShaderInstance madParticleShader;

    public static final RenderStateShard.ShaderStateShard particleShader = new RenderStateShard.ShaderStateShard(() -> MadParticleShader.madParticleShader);

    public static ShaderInstance getMadParticleShader() {
        return madParticleShader;
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        var resourceManager = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(resourceManager
                            , new ResourceLocation(MadParticle.MOD_ID, "particle"), MadParticleRenderTypes.PARTICLE)
                    , (shaderInstance -> madParticleShader = shaderInstance));

        } catch (Exception e) {
            throw new RuntimeException("failed to load particle shader", e);
        }
    }
}
