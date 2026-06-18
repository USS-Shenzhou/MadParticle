package cn.ussshenzhou.madparticle.particle.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.WindowResizeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @author USS_Shenzhou
 */
public class OitRenderer {
    final NeoInstancedRenderManager manager;
    final TextureTarget accum, reveal;
    private final GpuBuffer vbo;

    public OitRenderer(NeoInstancedRenderManager manager) {
        this.manager = manager;
        var mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        this.accum = new TextureTarget("MadParticle OIT accum", mainTarget.width, mainTarget.height, false, false, GpuFormat.RGBA16_FLOAT);
        this.reveal = new TextureTarget("MadParticle OIT reveal", mainTarget.width, mainTarget.height, false, false, GpuFormat.R16_UNORM);
        this.vbo = RenderSystem.getDevice().createBuffer(() -> "MadParticle OIT Post VBO", GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_VERTEX, 120);
        try (var mappedVbo = vbo.map(false, true)) {
            var buf = mappedVbo.data();
            buf.asFloatBuffer().put(new float[]{
                    1, -1, 1, 0,
                    1, 1, 1, 1,
                    -1, 1, 0, 1,
                    -1, -1, 0, 0
            });
        }
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onResize(WindowResizeEvent event) {
        this.accum.resize(event.getWindow().getWidth(), event.getWindow().getHeight());
        this.reveal.resize(event.getWindow().getWidth(), event.getWindow().getHeight());
    }

    void doRender(GpuTextureView color, GpuTextureView depth) {
        doRender0(color, depth);
        doRender1(color, depth);
    }

    @SuppressWarnings("DataFlowIssue")
    void doRender0(GpuTextureView color, GpuTextureView depth) {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var cameraUbo = manager.cameraCorrectionUbo.currentBuffer().map(false, true);
        var dynamicUbo = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        var mc = Minecraft.getInstance();
        var descriptor = RenderPassDescriptor.create(() -> "MadParticle OIT 0")
                .withColorAttachment(accum.getColorTextureView(), Optional.of(new Vector4f(0, 0, 0, 0)))
                .withColorAttachment(reveal.getColorTextureView(), Optional.of(new Vector4f(1, 0, 0, 0)))
                .withDepthAttachment(depth)
                .withRenderArea(new RenderPass.RenderArea(0, 0, color.getWidth(0), color.getHeight(0)));
        var pass = encoder.createRenderPass(descriptor);
        try (pass; cameraUbo) {
            pass.setPipeline(ModRenderPipelines.INSTANCED_OIT);
            manager.simpleRenderer.prepareUniformSampler(pass, mc, cameraUbo, dynamicUbo);
            manager.tickVBO.getCurrent().bind(0, pass);
            var ebo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            pass.setIndexBuffer(ebo.getBuffer(6).slice(0, 6).buffer(), ebo.type());
            pass.drawIndexed(6, manager.amount, 0, 0, 0);
        }
    }

    void doRender1(GpuTextureView color, GpuTextureView depth) {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var pass = encoder.createRenderPass(
                () -> "MadParticle OIT 1",
                color,
                Optional.empty(),
                depth,
                OptionalDouble.empty());
        try (pass) {
            pass.setPipeline(ModRenderPipelines.INSTANCED_OIT_POST);
            prepareUniformSampler(pass);
            pass.setVertexBuffer(0, vbo.slice());
            var ebo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            pass.setIndexBuffer(ebo.getBuffer(6).slice(0, 6).buffer(), ebo.type());
            pass.drawIndexed(6, 1, 0, 0, 0);
        }
    }

    void prepareUniformSampler(RenderPass pass) {
        pass.bindTexture("accum", accum.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        pass.bindTexture("reveal", reveal.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
    }
}
