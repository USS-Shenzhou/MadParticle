package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.t88.config.ConfigHelper;
//import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.lwjgl.opengl.GL40C.*;


/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ModParticleRenderTypes {

    public static final VertexFormat INSTANCED_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .build();

    public static final ParticleRenderType INSTANCED = (tesselator, pTextureManager) -> {
        RenderSystem.setShader(ModParticleShaders::getInstancedParticleShader);
        RenderSystem.depthMask(ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod == TranslucentMethod.DEPTH_TRUE);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //return new BufferBuilder(new ByteBufferBuilder(512 * 512), VertexFormat.Mode.QUADS, INSTANCED_FORMAT);
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, INSTANCED_FORMAT);
    };

    public static final ParticleRenderType INSTANCED_OIT = (tesselator, pTextureManager) -> {
        RenderSystem.setShader(ModParticleShaders::getInstancedParticleShaderOit);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunci(0, GL_ONE, GL_ONE);
        glBlendFunci(1, GL_ZERO, GL_ONE_MINUS_SRC_COLOR);
        glBlendEquation(GL_FUNC_ADD);
        //return new BufferBuilder(new ByteBufferBuilder(512 * 512), VertexFormat.Mode.QUADS, INSTANCED_FORMAT);
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, INSTANCED_FORMAT);
    };
}