package cn.ussshenzhou.madparticle.particle.optimize;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL33.*;

/**
 * @author USS_Shenzhou
 */
public class InstancedArrayBuffer {
    private long address = -1;
    private final int id;
    private int byteSize = 0;

    public InstancedArrayBuffer() {
        this.id = glGenBuffers();
    }

    public long getAddress() {
        return address;
    }

    public void alloc(int byteSize) {
        if (address != -1) {
            MemoryUtil.nmemFree(address);
        }
        this.byteSize = byteSize;
        address = MemoryUtil.nmemAlloc(byteSize);
        if (address == 0) {
            throw new OutOfMemoryError("Could not allocate " + byteSize + " bytes");
        }
    }

    public void free() {
        if (address != -1) {
            MemoryUtil.nmemFree(address);
        }
        byteSize = 0;
        address = -1;
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    public void update() {
        if (address == -1) {
            throw new IllegalStateException("The buffer has not been allocated.");
        }
        nglBufferData(GL_ARRAY_BUFFER, byteSize, address, GL_STREAM_DRAW);
    }
}
