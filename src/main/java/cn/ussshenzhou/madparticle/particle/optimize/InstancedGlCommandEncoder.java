package cn.ussshenzhou.madparticle.particle.optimize;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
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
            GL31.glDrawArraysInstanced(GlConst.toGl(pipeline.info().getVertexFormatMode()), first, count, instanceToDraw);
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
}
