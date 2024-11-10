package cn.ussshenzhou.madparticle.util;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

/**
 * @author USS_Shenzhou
 */
public class VoxelShapeHelper {
    private static final ThreadLocal<ArrayVoxelShape> SHARED_ARRAY_VOXEL_SHAPE = ThreadLocal.withInitial(() -> new ArrayVoxelShape(Shapes.block().shape, DoubleArrayList.wrap(new double[]{0, 0}), DoubleArrayList.wrap(new double[]{0, 0}), DoubleArrayList.wrap(new double[]{0, 0})));

    /**
     * @see Shapes#findBits(double, double)
     */
    private static int findBits(double pMinBits, double pMaxBits) {
        if (!(pMinBits < -1.0E-7D) && !(pMaxBits > 1.0000001D)) {
            for (int i = 0; i <= 3; ++i) {
                int j = 1 << i;
                double d0 = pMinBits * (double) j;
                double d1 = pMaxBits * (double) j;
                boolean flag = Math.abs(d0 - (double) Math.round(d0)) < 1.0E-7D * (double) j;
                boolean flag1 = Math.abs(d1 - (double) Math.round(d1)) < 1.0E-7D * (double) j;
                if (flag && flag1) {
                    return i;
                }
            }
            return -1;
        } else {
            return -1;
        }
    }

    private static ArrayVoxelShape setVoxelShape(ArrayVoxelShape thiz, double x0, double x1, double y0, double y1, double z0, double z1) {
        thiz.xs.set(0, x0);
        thiz.xs.set(1, x1);
        thiz.ys.set(0, y0);
        thiz.ys.set(1, y1);
        thiz.zs.set(0, z0);
        thiz.zs.set(1, z1);
        return thiz;
    }

    /**
     * @see Shapes#create(double, double, double, double, double, double)
     */
    private static VoxelShape create(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!(maxX - minX < 1.0E-7D) && !(maxY - minY < 1.0E-7D) && !(maxZ - minZ < 1.0E-7D)) {
            int i = findBits(minX, maxX);
            int j = findBits(minY, maxY);
            int k = findBits(minZ, maxZ);
            if (i >= 0 && j >= 0 && k >= 0) {
                if (i == 0 && j == 0 && k == 0) {
                    return Shapes.block();
                } else {
                    int l = 1 << i;
                    int i1 = 1 << j;
                    int j1 = 1 << k;
                    BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.withFilledBounds(l, i1, j1, (int) Math.round(minX * (double) l), (int) Math.round(minY * (double) i1), (int) Math.round(minZ * (double) j1), (int) Math.round(maxX * (double) l), (int) Math.round(maxY * (double) i1), (int) Math.round(maxZ * (double) j1));
                    return new CubeVoxelShape(bitSetDiscreteVoxelShape);
                }
            } else {
                return setVoxelShape(SHARED_ARRAY_VOXEL_SHAPE.get(), minX, maxX, minY, maxY, minZ, maxZ);
            }
        } else {
            return Shapes.empty();
        }
    }

    /**
     * @see Shapes#create(AABB)
     */
    static VoxelShape create(AABB aabb) {
        return create(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }
}
