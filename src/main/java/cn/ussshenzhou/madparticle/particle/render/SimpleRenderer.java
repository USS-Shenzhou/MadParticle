package cn.ussshenzhou.madparticle.particle.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @author USS_Shenzhou
 */
public class SimpleRenderer {

    final NeoInstancedRenderManager manager;

    public SimpleRenderer(NeoInstancedRenderManager manager) {
        this.manager = manager;
    }

    void doRender(GpuTextureView color, GpuTextureView depth, RenderPipeline pipeline) {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var cameraUbo = manager.cameraCorrectionUbo.currentBuffer().map(false, true);
        var dynamicUbo = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        var mc = Minecraft.getInstance();
        var pass = encoder.createRenderPass(
                () -> "MadParticle SimpleRenderer",
                color,
                Optional.empty(),
                depth,
                OptionalDouble.empty());
        try (pass; cameraUbo) {
            pass.setPipeline(pipeline);
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
}
