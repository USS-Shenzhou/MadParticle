package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author zomb-676
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = MadParticle.MOD_ID)
public class ModParticleShaders {

    //instance particle shader
    public static ShaderInstance instancedParticleShader;
    public static final RenderStateShard.ShaderStateShard INSTANCED_SHADER = new RenderStateShard.ShaderStateShard(() -> instancedParticleShader);

    public static ShaderInstance getInstancedParticleShader() {
        return instancedParticleShader;
    }

    public static ShaderInstance instancedParticleShaderOit;
    public static final RenderStateShard.ShaderStateShard INSTANCED_SHADER_OIT = new RenderStateShard.ShaderStateShard(() -> instancedParticleShaderOit);

    public static ShaderInstance getInstancedParticleShaderOit() {
        return instancedParticleShaderOit;
    }

    public static ShaderInstance instancedParticleShaderOitPost;
    public static final RenderStateShard.ShaderStateShard INSTANCED_SHADER_OIT_POST = new RenderStateShard.ShaderStateShard(() -> instancedParticleShaderOitPost);

    public static ShaderInstance getInstancedParticleShaderOitPost() {
        return instancedParticleShaderOitPost;
    }

    //bloom shader
    public static ShaderInstance downSamplerShader;

    public static ShaderInstance getDownSamplerShader() {
        return downSamplerShader;
    }

    public static ShaderInstance bloomCompositeShader;

    public static ShaderInstance getBloomCompositeShader() {
        return bloomCompositeShader;
    }

    public static ShaderInstance oitExtractShader;

    public static ShaderInstance getOitExtractShader() {
        return oitExtractShader;
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        var resourceManager = event.getResourceProvider();
        try {
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle"), ModParticleRenderTypes.INSTANCED_FORMAT),
                    shaderInstance -> instancedParticleShader = shaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"), ModParticleRenderTypes.INSTANCED_FORMAT),
                    shaderInstance -> instancedParticleShaderOit = shaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit_post"), DefaultVertexFormat.POSITION_TEX),
                    shaderInstance -> instancedParticleShaderOitPost = shaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "down_sampler"),DefaultVertexFormat.POSITION),
                            ShaderInstance -> downSamplerShader = ShaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "bloom_composite"),DefaultVertexFormat.POSITION),
                    ShaderInstance -> bloomCompositeShader = ShaderInstance
            );
            event.registerShader(
                    new ShaderInstance(resourceManager,
                            ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "extract_oit"),DefaultVertexFormat.POSITION),
                    ShaderInstance -> oitExtractShader = ShaderInstance
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to load particle shader", e);
        }
    }
}
