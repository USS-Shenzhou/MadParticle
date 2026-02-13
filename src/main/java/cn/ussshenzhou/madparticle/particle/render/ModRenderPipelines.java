package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * @author USS_Shenzhou
 */
public class ModRenderPipelines {
    public static final VertexFormat INSTANCED_VERTEX_FORMAT = VertexFormat.builder()
            .add("MadParticle Position", VertexFormatElement.POSITION)
            .build();

    public static final RenderPipeline.Snippet INSTANCED_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withVertexFormat(INSTANCED_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withVertexShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withUniform("CameraCorrection", UniformType.UNIFORM_BUFFER)
            .withPolygonMode(PolygonMode.FILL)
            .withCull(true)
            .withColorWrite(true, true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .buildSnippet();

    public static final RenderPipeline INSTANCED_COMMON_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(true)
                    .build()
    );

    public static final RenderPipeline INSTANCED_COMMON_BLEND = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common_blend"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    .build()
    );

    public static final RenderPipeline INSTANCED_OIT = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_oit"))
                    .withVertexShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"))
                    .withBlend(BlendFunction.ADDITIVE)
                    .withDepthWrite(false)
                    .build()
    );

    //public static final RenderPipeline INSTANCED_OIT_POST = RenderPipelines.register(
    //        RenderPipeline.builder()
    //                .withVertexFormat(, VertexFormat.Mode.QUADS)
    //                .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_oit_post"))
    //                .withVertexShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit_post"))
    //                .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit_post"))
    //                .withColorWrite(true, true)
    //                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
    //                .withBlend(BlendFunction.TRANSLUCENT)
    //                .withDepthWrite(true)
    //                .build()
    //);
}
