package cn.ussshenzhou.madparticle.particle.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.*;

/**
 * @author USS_Shenzhou
 */
public class PersistentMappedArrayBuffer {
    private final ArrayList<PersistentMappedBuffer> buffers = new ArrayList<>();
    private PersistentMappedBuffer lastUsing = null;
    protected int byteSize, usingIndex = 0;
    private static final int N = 4;

    public PersistentMappedArrayBuffer() {
        ensureCapacity(1024 * 1024);
    }

    public void ensureCapacity(int byteSize) {
        if (byteSize > this.byteSize) {
            if (lastUsing != null) {
                lastUsing.free();
            }
            if (!buffers.isEmpty()) {
                lastUsing = buffers.get(usingIndex);
            }
            for (var buffer : buffers) {
                if (buffer != lastUsing) {
                    buffer.free();
                }
            }
            buffers.clear();
            this.byteSize = (int) (byteSize * 1.25);
            buffers.addAll(IntStream.range(0, N).mapToObj(i -> new PersistentMappedBuffer(this.byteSize)).toList());
            usingIndex = 0;
        }
    }

    public PersistentMappedBuffer getNext() {
        return buffers.get((usingIndex + 1) % N);
    }

    public PersistentMappedBuffer getCurrent() {
        if (lastUsing != null) {
            return lastUsing;
        }
        return buffers.get(usingIndex);
    }

    public void next() {
        if (lastUsing != null) {
            lastUsing.free();
            lastUsing = null;
        }
        usingIndex = (usingIndex + 1) % N;
    }

    public static class PersistentMappedBuffer {
        private final GpuBuffer gpuBuffer;
        private GpuBuffer.MappedView mappedBuffer = null;
        private long address = -1;

        public PersistentMappedBuffer(int byteSize) {
            gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "MadParticle VBO", GpuBuffer.USAGE_MAP_WRITE, byteSize);
        }

        public void prepare() {
            if (mappedBuffer == null) {
                mappedBuffer = RenderSystem.getDevice().backend.createCommandEncoder().mapBuffer(gpuBuffer.slice(), false, true);
            }
            address = MemoryUtil.memAddress(mappedBuffer.data());
        }

        public long getMappedAddress() {
            if (address == -1) {
                throw new IllegalStateException("Not prepared");
            }
            return address;
        }

        public void done() {
            if (mappedBuffer != null) {
                mappedBuffer.close();
            }
            mappedBuffer = null;
            address = -1;
        }

        public void free() {
            if (mappedBuffer != null) {
                mappedBuffer.close();
            }
            gpuBuffer.close();
            address = -1;
        }

        public void bind() {
            glBindBuffer(GL_ARRAY_BUFFER, ((GlBuffer) (gpuBuffer)).handle);
        }

    }
}
