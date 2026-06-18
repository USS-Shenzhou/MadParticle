package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
public class ModRenderPipelines {
    public static final VertexFormat INSTANCED_VERTEX_FORMAT = VertexFormat.builder(1)
            .addAttribute("instanceXYZRoll", GpuFormat.RGBA32_FLOAT)
            .addAttribute("prevInstanceXYZRoll", GpuFormat.RGBA32_FLOAT)
            .addAttribute("instanceUV", GpuFormat.RGBA16_FLOAT)
            .addAttribute("instanceColor", GpuFormat.RGBA16_FLOAT)
            .addAttribute("sizeExtraLight", GpuFormat.RG16_FLOAT)
            .addAttribute("instanceUV2", GpuFormat.R32_UINT)
            .build();

    public static final BindGroupLayout CAMERA_CORRECTION = BindGroupLayout.builder().withUniform("CameraCorrection", UniformType.UNIFORM_BUFFER).build();

    public static final RenderPipeline.Snippet INSTANCED_SNIPPET = RenderPipeline.builder()
            .withVertexShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle"))

            .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
            .withBindGroupLayout(CAMERA_CORRECTION)
            .withBindGroupLayout(BindGroupLayouts.FOG)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)

            .withVertexBinding(0, INSTANCED_VERTEX_FORMAT)

            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withPolygonMode(PolygonMode.FILL)
            .withCull(true)
            .buildSnippet();

    public static final RenderPipeline INSTANCED_COMMON_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                    .build()
    );

    public static final RenderPipeline INSTANCED_COMMON_BLEND = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_common_blend"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_common"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build()
    );

    public static final RenderPipeline INSTANCED_OIT = RenderPipelines.register(
            RenderPipeline.builder(INSTANCED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_oit"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit"))
                    .withColorTargetState(0, new ColorTargetState(Optional.of(BlendFunction.ADDITIVE), GpuFormat.RGBA32_FLOAT, ColorTargetState.WRITE_ALL))
                    .withColorTargetState(1, new ColorTargetState(Optional.of(new BlendFunction(BlendFactor.ZERO, BlendFactor.ONE_MINUS_SRC_COLOR)), GpuFormat.R16_FLOAT, ColorTargetState.WRITE_ALL))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build()
    );

    public static final VertexFormat OIT_POST_VERTEX_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RG32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .build();

    public static final BindGroupLayout ACCUM = BindGroupLayout.builder().withSampler("accum").build();
    public static final BindGroupLayout REVEAL = BindGroupLayout.builder().withSampler("reveal").build();

    public static final RenderPipeline INSTANCED_OIT_POST = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_oit_post"))
                    .withVertexShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit_post"))

                    .withVertexBinding(0, OIT_POST_VERTEX_FORMAT)

                    .withFragmentShader(Identifier.fromNamespaceAndPath(MadParticle.MOD_ID, "instanced_particle_oit_post"))
                    .withBindGroupLayout(ACCUM)
                    .withBindGroupLayout(REVEAL)
                    .withColorTargetState(0, new ColorTargetState(Optional.of(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withPolygonMode(PolygonMode.FILL)
                    .build()
    );
}
