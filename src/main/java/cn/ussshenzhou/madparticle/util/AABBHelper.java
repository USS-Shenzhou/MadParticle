package cn.ussshenzhou.madparticle.util;

import net.minecraft.world.phys.AABB;

/**
 * @author USS_Shenzhou
 */
public class AABBHelper {

    public static AABB move(AABB thiz, double dx, double dy, double dz) {
        thiz.minX += dx;
        thiz.minY += dy;
        thiz.minZ += dz;
        thiz.maxX += dx;
        thiz.maxY += dy;
        thiz.maxZ += dz;
        return thiz;
    }

    public static AABB set(AABB thiz, double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
        thiz.minX = Math.min(pX1, pX2);
        thiz.minY = Math.min(pY1, pY2);
        thiz.minZ = Math.min(pZ1, pZ2);
        thiz.maxX = Math.max(pX1, pX2);
        thiz.maxY = Math.max(pY1, pY2);
        thiz.maxZ = Math.max(pZ1, pZ2);
        return thiz;
    }

    public static AABB set(AABB thiz, AABB that) {
        thiz.minX = that.minX;
        thiz.minY = that.minY;
        thiz.minZ = that.minZ;
        thiz.maxX = that.maxX;
        thiz.maxY = that.maxY;
        thiz.maxZ = that.maxZ;
        return thiz;
    }

    public static AABB expandTowards(AABB thiz, double pX, double pY, double pZ) {
        if (pX < 0) {
            thiz.minX += pX;
        } else {
            thiz.maxX += pX;
        }
        if (pY < 0) {
            thiz.minY += pY;
        } else {
            thiz.maxY += pY;
        }
        if (pZ < 0) {
            thiz.minZ += pZ;
        } else {
            thiz.maxZ += pZ;
        }
        return thiz;
    }
}
