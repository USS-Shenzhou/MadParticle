package cn.ussshenzhou.madparticle.util;

/**
 * @author USS_Shenzhou
 */
public class SimpleBlockPos {
    public int x, y, z;

    public SimpleBlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SimpleBlockPos copy() {
        return new SimpleBlockPos(x, y, z);
    }

    @Override
    public int hashCode() {
        return (y + z * 31) * 37 + x;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SimpleBlockPos pos)) {
            return false;
        } else {
            return this.x == pos.x && this.y == pos.y && this.z == pos.z;
        }
    }
}
