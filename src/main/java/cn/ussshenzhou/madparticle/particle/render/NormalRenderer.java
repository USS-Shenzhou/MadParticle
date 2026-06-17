package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @author USS_Shenzhou
 */
public class NormalRenderer {

    private final NeoInstancedRenderManager manager;

    public NormalRenderer(NeoInstancedRenderManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("DataFlowIssue")
    void doRender(CommandEncoder encoder, GpuBufferSlice.MappedView cameraUbo, GpuBufferSlice dynamicUbo) {
        var mc = Minecraft.getInstance();
        var pass = encoder.createRenderPass(
                () -> "MadParticle render",
                mc.gameRenderer.mainRenderTarget().getColorTextureView(),
                Optional.empty(),
                mc.gameRenderer.mainRenderTarget().getDepthTextureView(),
                OptionalDouble.empty());
        try (pass; cameraUbo) {
            pass.setPipeline(getRenderPipeline());
            prepareUniformSampler(pass, mc, cameraUbo, dynamicUbo);
            manager.tickVBO.getCurrent().bind(0, pass);
            var ebo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            pass.setIndexBuffer(ebo.getBuffer(6).slice(0, 6).buffer(), ebo.type());
            pass.drawIndexed(6, manager.amount, 0, 0, 0);
        }
    }

    void prepareUniformSampler(RenderPass pass, Minecraft mc, GpuBufferSlice.MappedView ubo, GpuBufferSlice dynamicUbo) {
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", dynamicUbo);
        var camera = mc.gameRenderer.mainCamera();
        var cameraPos = camera.position();
        var cameraRot = camera.rotation();
        Std140Builder.intoBuffer(ubo.data())
                .putVec4(cameraRot.x, cameraRot.y, cameraRot.z, cameraRot.w)
                .putVec4((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        pass.setUniform("CameraCorrection", manager.cameraCorrectionUbo.currentBuffer());
        var texture = mc.getTextureManager().getTexture(manager.usingAtlas);
        pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
        pass.bindTexture("Sampler2", mc.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
    }

    private static RenderPipeline getRenderPipeline() {
        return switch (ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod) {
            case DEPTH_TRUE -> ModRenderPipelines.INSTANCED_COMMON_DEPTH;
            case DEPTH_FALSE -> ModRenderPipelines.INSTANCED_COMMON_BLEND;
            default -> ModRenderPipelines.INSTANCED_COMMON_DEPTH;
        };
    }
}
