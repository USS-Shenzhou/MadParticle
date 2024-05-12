package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/**
 * @author USS_Shenzhou
 */
public class InstancedRenderBufferBuilder extends BufferBuilder {
    public InstancedRenderBufferBuilder(int pCapacity) {
        super(pCapacity);
    }


    public InstancedRenderBufferBuilder uvControl(int u0, int u1, int v0, int v1) {
        VertexFormatElement vertexformatelement = this.currentElement();
        if (vertexformatelement.getUsage() != ModParticleRenderTypes.UV_CONTROL) {
            return this;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.INT && vertexformatelement.getCount() == 4) {
            this.putByte(0, (byte) u0);
            this.putByte(4, (byte) u1);
            this.putByte(2 * 4, (byte) v0);
            this.putByte(3 * 4, (byte) v1);
            this.nextElement();
            return this;
        } else {
            throw new IllegalStateException();
        }
    }
}
