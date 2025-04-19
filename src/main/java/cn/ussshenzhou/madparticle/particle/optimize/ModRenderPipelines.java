package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

/**
 * @author USS_Shenzhou
 */
public class ModRenderPipelines {
    public static final VertexFormat INSTANCED_VERTEX_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .build();

    public static final RenderPipeline.Snippet INSTANCED_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_FOG_SNIPPET)
            .withVertexShader(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle"))
            .withVertexFormat(INSTANCED_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withUniform("CamXYZ", UniformType.VEC3)
            .withUniform("CamQuat", UniformType.VEC4)
            .withPolygonMode(PolygonMode.FILL)
            .withCull(true)
            .withColorLogic(LogicOp.NONE)
            .buildSnippet();

    public static final RenderPipeline INSTANCED_COMMON_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withColorWrite(true, true)
                    .withDepthWrite(true)
                    .build()
    );

    public static final RenderPipeline INSTANCED_COMMON_BLEND = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withColorWrite(true, true)
                    .withDepthWrite(true)
                    .build()
    );

    public static final RenderPipeline INSTANCED_OIT = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.ADDITIVE)
                    .withColorWrite(true, true)
                    .withDepthWrite(false)
                    .build()
    );

    //TODO
    /*public static final RenderPipeline INSTANCED_OIT_POST = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_FOG_SNIPPET)
                    .withLocation(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.ADDITIVE)
                    .withColorWrite(true, true)
                    .withDepthWrite(false)
                    .build()
    );*/
}
