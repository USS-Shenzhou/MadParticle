package cn.ussshenzhou.madparticle.particle.optimize;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.ARBDirectStateAccess.glUnmapNamedBuffer;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.ARBBufferStorage.*;

/**
 * @author USS_Shenzhou
 */
public class InstancedArrayBuffer {
    private PersistentMappedBuffer usingBuffer = null;
    private final ArrayList<PersistentMappedBuffer> buffers = new ArrayList<>();

    public InstancedArrayBuffer() {
    }

    public long getAddress() {
        if (usingBuffer == null) {
            throw new IllegalStateException("The buffer has not been allocated.");
        }
        return usingBuffer.getAddress();
    }

    public void ensureCapacity(int byteSize) {
        PersistentMappedBuffer buffer = null;
        var iterator = buffers.iterator();
        while (iterator.hasNext()) {
            var buf = iterator.next();
            if (buf.byteSize >= byteSize && buf.usable()) {
                buffer = buf;
            } else if (buf.byteSize < byteSize && buf != usingBuffer) {
                buf.free();
                iterator.remove();
            }
        }
        if (buffer == null) {
            usingBuffer = new PersistentMappedBuffer(byteSize);
            buffers.add(usingBuffer);
        }
    }

    public void bind() {
        usingBuffer.bind();
    }

    public void using() {
        usingBuffer.using();
    }

    public static class PersistentMappedBuffer {
        private ByteBuffer mappedBuffer;
        private final int id, byteSize;
        private long fence = -1;

        public PersistentMappedBuffer(int byteSize) {
            this.id = glGenBuffers();
            this.byteSize = byteSize;
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

        public void using() {
            fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            if (fence == 0){
                throw new RuntimeException("Failed to create GL fence");
            }
        }

        public boolean usable() {
            if (fence == -1) {
                return true;
            }
            if (glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, 0) == GL_ALREADY_SIGNALED) {
                glDeleteSync(fence);
                fence = -1;
                return true;
            }
            return false;
        }

    }
}
