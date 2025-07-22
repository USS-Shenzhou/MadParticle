package cn.ussshenzhou.madparticle.particle.optimize;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.ARBDirectStateAccess.glUnmapNamedBuffer;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.ARBBufferStorage.*;

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
        private ByteBuffer mappedBuffer;
        private final int id;

        public PersistentMappedBuffer(int byteSize) {
            this.id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            int flag = GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT | GL_MAP_PERSISTENT_BIT;
            glBufferStorage(GL_ARRAY_BUFFER, byteSize, flag);
            mappedBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, byteSize, flag);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        public long getAddress() {
            return MemoryUtil.memAddress(mappedBuffer);
        }

        public void free() {
            glUnmapNamedBuffer(id);
            glDeleteBuffers(id);
            mappedBuffer = null;
        }

        public void bind() {
            glBindBuffer(GL_ARRAY_BUFFER, id);
        }

    }
}
