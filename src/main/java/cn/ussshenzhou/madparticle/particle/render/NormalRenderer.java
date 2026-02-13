package cn.ussshenzhou.madparticle.particle.render;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static cn.ussshenzhou.madparticle.particle.render.NeoInstancedRenderManager.*;
import static org.lwjgl.opengl.ARBInstancedArrays.glVertexAttribDivisorARB;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

/**
 * @author USS_Shenzhou
 */
public class NormalRenderer {

    private final NeoInstancedRenderManager manager;

    public NormalRenderer(NeoInstancedRenderManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("DataFlowIssue")
    void doRender(CommandEncoder encoder, GpuBuffer.MappedView cameraUbo, GpuBufferSlice dynamicUbo) {
        var mc = Minecraft.getInstance();
        var pass = encoder.createRenderPass(
                () -> "MadParticle render",
                mc.getMainRenderTarget().getColorTextureView(),
                OptionalInt.empty(),
                mc.getMainRenderTarget().getDepthTextureView(),
                OptionalDouble.empty());
        try (pass; cameraUbo) {
            pass.setPipeline(getRenderPipeline());
            prepareUniformSampler(pass, mc, cameraUbo, dynamicUbo);
            setVAO(pass);
            manager.tickVBO.getCurrent().bind();
            pass.setIndexBuffer(EBO, VertexFormat.IndexType.INT);
            //manager.bindIrisFBO();
            pass.drawIndexed(0, 0, 6, manager.amount);
        }
    }

    void setVAO(RenderPass pass) {
        pass.setVertexBuffer(0, PROXY_VAO);
        getDevice().vertexArrayCache().bindVertexArray(getRenderPipeline().getVertexFormat(), (GlBuffer) PROXY_VAO);
        setVertexAttributeArray();
    }

    void setVertexAttributeArray() {
        manager.tickVBO.getCurrent().bind();
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, TICK_VBO_SIZE, 0);
        glVertexAttribDivisorARB(0, 1);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, TICK_VBO_SIZE, 16);
        glVertexAttribDivisorARB(1, 1);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 32);
        glVertexAttribDivisorARB(2, 1);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 40);
        glVertexAttribDivisorARB(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_HALF_FLOAT, false, TICK_VBO_SIZE, 48);
        glVertexAttribDivisorARB(4, 1);

        glEnableVertexAttribArray(5);
        glVertexAttribIPointer(5, 1, GL_UNSIGNED_BYTE, TICK_VBO_SIZE, 52);
        glVertexAttribDivisorARB(5, 1);
    }

    void prepareUniformSampler(RenderPass pass, Minecraft mc, GpuBuffer.MappedView ubo, GpuBufferSlice dynamicUbo) {
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", dynamicUbo);
        var camera = mc.gameRenderer.getMainCamera();
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
