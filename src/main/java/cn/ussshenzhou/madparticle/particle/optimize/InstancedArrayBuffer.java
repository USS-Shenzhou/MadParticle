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
public class InstancedArrayBuffer {
    private final ArrayList<PersistentMappedBuffer> buffers = new ArrayList<>();
    private int byteSize, usingIndex = -1;

    public InstancedArrayBuffer() {
    }

    public long getAddress() {
        if (usingIndex == -1) {
            throw new IllegalStateException("The buffer has not been allocated.");
        }
        return buffers.get(usingIndex).getAddress();
    }

    public void ensureCapacity(int byteSize) {
        if (byteSize <= this.byteSize) {
            usingIndex = (usingIndex + 1) % buffers.size();
        } else {
            buffers.forEach(PersistentMappedBuffer::free);
            buffers.clear();
            buffers.addAll(IntStream.range(0, 6).mapToObj(i -> new PersistentMappedBuffer(byteSize)).toList());
            usingIndex = 0;
            this.byteSize = byteSize;
        }
    }

    public void bind() {
        buffers.get(usingIndex).bind();
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
