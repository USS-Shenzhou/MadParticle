package cn.ussshenzhou.madparticle.particle.optimize;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL31;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class InstancedGlCommandEncoder extends GlCommandEncoder {
    private static InstancedGlCommandEncoder instancedGlCommandEncoder = null;
    private static int instanceToDraw = -1;

    public static InstancedGlCommandEncoder getInstance() {
        if (instancedGlCommandEncoder == null) {
            instancedGlCommandEncoder = new InstancedGlCommandEncoder((GlDevice) RenderSystem.getDevice());
        }
        return instancedGlCommandEncoder;
    }

    protected InstancedGlCommandEncoder(GlDevice device) {
        super(device);
    }

    @Override
    public void drawFromBuffers(GlRenderPass renderPass, int first, int count, @Nullable VertexFormat.IndexType indexType, GlRenderPipeline pipeline) {
        if (indexType != null) {
            throw new UnsupportedOperationException();
        } else {
            if (instanceToDraw == -1) {
                throw new IllegalStateException("instanceToDraw not updated");
            }
            GL31.glDrawArraysInstanced(GL31.GL_TRIANGLE_STRIP, first, count, instanceToDraw);
            instanceToDraw = -1;
        }
    }

    public static void setInstanceToDraw(int instanceToDraw) {
        InstancedGlCommandEncoder.instanceToDraw = instanceToDraw;
    }

    @Override
    protected void executeDraw(GlRenderPass renderPass, int first, int count, @Nullable VertexFormat.IndexType indexType) {
        if (this.trySetup(renderPass)) {
            if (GlRenderPass.VALIDATION) {
                if (indexType != null) {
                    if (renderPass.indexBuffer == null) {
                        throw new IllegalStateException("Missing index buffer");
                    }

                    if (renderPass.indexBuffer.isClosed()) {
                        throw new IllegalStateException("Index buffer has been closed!");
                    }
                }

                /*if (renderPass.vertexBuffers[0] == null) {
                    throw new IllegalStateException("Missing vertex buffer at slot 0");
                }

                if (renderPass.vertexBuffers[0].isClosed()) {
                    throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
                }*/
            }

            this.drawFromBuffers(renderPass, first, count, indexType, renderPass.pipeline);
        }
    }

    @Override
    public boolean trySetup(GlRenderPass renderPass) {
        if (GlRenderPass.VALIDATION) {
            if (renderPass.pipeline == null) {
                throw new IllegalStateException("Can't draw without a render pipeline");
            }

            if (renderPass.pipeline.program() == GlProgram.INVALID_PROGRAM) {
                throw new IllegalStateException("Pipeline contains invalid shader program");
            }

            for (RenderPipeline.UniformDescription renderpipeline$uniformdescription : renderPass.pipeline.info().getUniforms()) {
                Object object = renderPass.uniforms.get(renderpipeline$uniformdescription.name());
                if (object == null && !GlProgram.BUILT_IN_UNIFORMS.contains(renderpipeline$uniformdescription.name())) {
                    throw new IllegalStateException(
                            "Missing uniform " + renderpipeline$uniformdescription.name() + " (should be " + renderpipeline$uniformdescription.type() + ")"
                    );
                }
            }

            for (String s1 : renderPass.pipeline.program().getSamplers()) {
                if (!renderPass.samplers.containsKey(s1)) {
                    throw new IllegalStateException("Missing sampler " + s1);
                }

                if (renderPass.samplers.get(s1).isClosed()) {
                    throw new IllegalStateException("Sampler " + s1 + " has been closed!");
                }
            }

            if (renderPass.pipeline.info().wantsDepthTexture() && !renderPass.hasDepthTexture()) {
                LOGGER.warn("Render pipeline {} wants a depth texture but none was provided - this is probably a bug", renderPass.pipeline.info().getLocation());
            }
        } else if (renderPass.pipeline == null || renderPass.pipeline.program() == GlProgram.INVALID_PROGRAM) {
            return false;
        }

        RenderPipeline renderpipeline = renderPass.pipeline.info();
        GlProgram glprogram = renderPass.pipeline.program();

        for (Uniform uniform : glprogram.getUniforms()) {
            if (renderPass.dirtyUniforms.contains(uniform.getName())) {
                Object object1 = renderPass.uniforms.get(uniform.getName());
                if (object1 instanceof int[]) {
                    glprogram.safeGetUniform(uniform.getName()).set((int[])object1);
                } else if (object1 instanceof float[]) {
                    glprogram.safeGetUniform(uniform.getName()).set((float[])object1);
                } else if (object1 != null) {
                    throw new IllegalStateException("Unknown uniform type - expected " + uniform.getType() + ", found " + object1);
                }
            }
        }

        renderPass.dirtyUniforms.clear();
        this.applyPipelineState(renderpipeline);
        GlStateManager._glUseProgram(glprogram.getProgramId());


        IntList intlist = glprogram.getSamplerLocations();

        for (int j = 0; j < glprogram.getSamplers().size(); j++) {
            String s = glprogram.getSamplers().get(j);
            GlTexture gltexture = (GlTexture)renderPass.samplers.get(s);
            if (gltexture != null) {
                int i = intlist.getInt(j);
                Uniform.uploadInteger(i, j);
                GlStateManager._activeTexture(33984 + j);

                GlStateManager._bindTexture(gltexture.glId());
                gltexture.flushModeChanges();
            }
        }

        Window window = Minecraft.getInstance() == null ? null : Minecraft.getInstance().getWindow();
        glprogram.setDefaultUniforms(
                renderpipeline.getVertexFormatMode(),
                RenderSystem.getModelViewMatrix(),
                RenderSystem.getProjectionMatrix(),
                window == null ? 0.0F : window.getWidth(),
                window == null ? 0.0F : window.getHeight()
        );

        for (Uniform uniform1 : glprogram.getUniforms()) {
            uniform1.upload();
        }

        if (renderPass.scissorState.isEnabled()) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(
                    renderPass.scissorState.getX(), renderPass.scissorState.getY(), renderPass.scissorState.getWidth(), renderPass.scissorState.getHeight()
            );
        } else {
            GlStateManager._disableScissorTest();
        }

        var stencilTest = renderPass.stencilTest;
        if (stencilTest != null) {
            GlStateManager._enableStencilTest();

            var front = stencilTest.front();
            var back = stencilTest.back();
            if (front.equals(back)) {
                GlStateManager._stencilFunc(GlConst.toGl(front.compare()), stencilTest.referenceValue(), stencilTest.readMask());
                GlStateManager._stencilOp(GlConst.toGl(front.fail()), GlConst.toGl(front.depthFail()), GlConst.toGl(front.pass()));
            } else {
                GlStateManager._stencilFuncFront(GlConst.toGl(front.compare()), stencilTest.referenceValue(), stencilTest.readMask());
                GlStateManager._stencilFuncBack(GlConst.toGl(back.compare()), stencilTest.referenceValue(), stencilTest.readMask());
                GlStateManager._stencilOpFront(GlConst.toGl(front.fail()), GlConst.toGl(front.depthFail()), GlConst.toGl(front.pass()));
                GlStateManager._stencilOpBack(GlConst.toGl(back.fail()), GlConst.toGl(back.depthFail()), GlConst.toGl(back.pass()));
            }
            GlStateManager._stencilMask(stencilTest.writeMask());
        } else {
            GlStateManager._disableStencilTest();
        }

        return true;
    }
}
