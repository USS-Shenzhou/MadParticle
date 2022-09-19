package cn.ussshenzhou.madparticle.designer.universal.util;

/**
 * @author USS_Shenzhou
 */
public class Vec2i {
    public int x, y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vec2i vec2i) {
        x += vec2i.x;
        y += vec2i.y;
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public Vec2i copy() {
        return new Vec2i(x, y);
    }
}
