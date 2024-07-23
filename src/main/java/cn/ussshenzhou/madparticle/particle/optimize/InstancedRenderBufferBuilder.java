package cn.ussshenzhou.madparticle.particle.optimize;

import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.lwjgl.system.MemoryUtil;

/**
 * @author USS_Shenzhou
 */
public class InstancedRenderBufferBuilder extends BufferBuilder {

    public InstancedRenderBufferBuilder(ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format) {
        super(buffer, mode, format);
    }

    public InstancedRenderBufferBuilder uvControl(int u0, int u1, int v0, int v1) {
        long i = this.beginElement(ModParticleRenderTypes.ELEMENT_UV_CONTROL);
        if (i != -1L) {
            MemoryUtil.memPutByte(i, (byte) u0);
            MemoryUtil.memPutByte(i + 1, (byte) u1);
            MemoryUtil.memPutByte(i + 2, (byte) v0);
            MemoryUtil.memPutByte(i + 3, (byte) v1);
        }
        return this;
    }
}
