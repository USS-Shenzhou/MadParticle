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
public class ModParticleShaders {

    public static ShaderInstance traditionalParticleShader;
    public static final RenderStateShard.ShaderStateShard TRADITIONAL_SHADER = new RenderStateShard.ShaderStateShard(() -> ModParticleShaders.traditionalParticleShader);

    public static ShaderInstance getTraditionalParticleShader() {
        return traditionalParticleShader;
    }

    public static ShaderInstance instancedShader;
    public static final RenderStateShard.ShaderStateShard INSTANCED_SHADER = new RenderStateShard.ShaderStateShard(() -> instancedShader);

    public static ShaderInstance getInstancedShader() {
        return instancedShader;
    }


    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        var resourceManager = event.getResourceProvider();
        try {
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            new ResourceLocation(MadParticle.MOD_ID, "particle"), ModParticleRenderTypes.Traditional.TRADITIONAL_FORMAT),
                    shaderInstance -> traditionalParticleShader = shaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            new ResourceLocation(MadParticle.MOD_ID, "instanced_particle"), ModParticleRenderTypes.INSTANCED_FORMAT),
                    shaderInstance -> instancedShader = shaderInstance
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to load particle shader", e);
        }
    }
}
