package cn.usshenzhou.madparticle.particle;

import cn.usshenzhou.madparticle.Madparticle;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Consumer;

/**
 * @author zomb-676
 */
public class MadParticleShader {

    public static ShaderInstance madParticleShader;
    public static final String shaderName = "mad_particle";

    public static final RenderStateShard.ShaderStateShard particleShader = new RenderStateShard.ShaderStateShard(() -> MadParticleShader.madParticleShader);

    public static ShaderInstance getMadParticleShader() {
        return madParticleShader;
    }

    public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShader(ResourceManager resourceManager) {
        try {
            return new Pair<>(new ShaderInstance(resourceManager, shaderName, MadParticleRenderTypes.PARTICLE),
                    shaderInstance -> madParticleShader = shaderInstance);
        } catch (Exception e) {
            throw new RuntimeException("failed to load particle shader " + shaderName, e);
        }
    }
}
