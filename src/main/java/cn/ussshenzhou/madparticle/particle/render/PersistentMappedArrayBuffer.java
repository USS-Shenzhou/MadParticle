package cn.ussshenzhou.madparticle.particle.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
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
    protected int byteSize, usingIndex = -1;
    private static final int N = 4;
    private boolean trapping = false;

    public PersistentMappedArrayBuffer() {
    }

    public void ensureCapacity(int byteSize) {
        PersistentMappedBuffer using = usingIndex == -1 ? null : buffers.get(usingIndex);
        if (byteSize > this.byteSize) {
            buffers.forEach(persistentMappedBuffer -> {
                if (persistentMappedBuffer != using) {
                    persistentMappedBuffer.free();
                }
            });
            buffers.clear();
            this.byteSize = (int) (byteSize * 1.25);
            buffers.addAll(IntStream.range(0, N).mapToObj(i -> new PersistentMappedBuffer(this.byteSize)).toList());
            if (using != null) {
                buffers.add(using);
                trapping = true;
            }
        }
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public PersistentMappedBuffer getNext() {
        return trapping ? buffers.get(0) : buffers.get((usingIndex + 1) % N);
    }

    public PersistentMappedBuffer getCurrent() {
        return trapping ? buffers.get(N) : buffers.get(usingIndex);
    }

    public void next() {
        if (trapping) {
            trapping = false;
            usingIndex = 0;
            return;
        }
        usingIndex = (usingIndex + 1) % N;
    }

    public static class PersistentMappedBuffer {
        private final GpuBuffer gpuBuffer;
        private GpuBuffer.MappedView mappedBuffer = null;

        public PersistentMappedBuffer(int byteSize) {
            gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "MadParticle VBO", GpuBuffer.USAGE_MAP_WRITE, byteSize);
        }

        public long getMappedAddress() {
            if (mappedBuffer == null) {
                mappedBuffer = RenderSystem.getDevice().createCommandEncoder().mapBuffer(gpuBuffer, false, true);
            }
            return MemoryUtil.memAddress(mappedBuffer.data());
        }

        public void done() {
            if (mappedBuffer != null) {
                mappedBuffer.close();
            }
            mappedBuffer = null;
        }

        public void free() {
            if (mappedBuffer != null) {
                mappedBuffer.close();
            }
            gpuBuffer.close();
        }

        public void bind() {
            glBindBuffer(GL_ARRAY_BUFFER, ((GlBuffer) (gpuBuffer)).handle);
        }

    }
}
