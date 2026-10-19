package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class MovementHelper {
    public record Shared(AABB aabb,
                         BlockPos.MutableBlockPos sharedPos,
                         Cursor3D cursor3D,
                         ArrayList<VoxelShape> shapesList,
                         ArrayVoxelShape shape) {

        public Shared(AABB aabb, BlockPos.MutableBlockPos sharedPos, Cursor3D cursor3D, ArrayList<VoxelShape> shapesList, ArrayVoxelShape shape) {
            this.aabb = aabb;
            this.sharedPos = sharedPos;
            this.cursor3D = cursor3D;
            this.shapesList = shapesList;
            this.shape = shape;
        }

        public Shared() {
            this(new AABB(0, 0, 0, 0, 0, 0),
                    new BlockPos.MutableBlockPos(),
                    new Cursor3D(0, 0, 0, 0, 0, 0),
                    new ArrayList<>(),
                    new ArrayVoxelShape(Shapes.block().shape, DoubleArrayList.wrap(new double[]{0, 0}), DoubleArrayList.wrap(new double[]{0, 0}), DoubleArrayList.wrap(new double[]{0, 0})));
        }
    }

    private static final ThreadLocal<Shared> SHARED = ThreadLocal.withInitial(Shared::new);
    private static final PositionedCache<BlockState> BLOCK_STATE_CACHE = new PositionedCache<>(ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheXZRange,
            ConfigHelper.getConfigRead(MadParticleConfig.class).lightCacheYRange,
            null);

    /**
     * @see Entity#collideBoundingBox(Entity, Vec3, AABB, Level, List)
     */
    public static @Nullable Vec3 collideBoundingBox(double dx, double dy, double dz, AABB collisionBox, Level level) {
        var shared = SHARED.get();
        var collisionBoxShared = AABBHelper.set(shared.aabb, collisionBox);
        shared.shapesList.clear();

        getBlockCollisions(shared, level, AABBHelper.expandTowards(collisionBoxShared, dx, dy, dz));
        AABBHelper.set(collisionBoxShared, collisionBox);
        return collideWithShapes(dx, dy, dz, collisionBoxShared, shared.shapesList);
    }

    /**
     * @see net.minecraft.world.level.BlockCollisions
     */
    public static void getBlockCollisions(Shared shared, Level level, AABB box) {
        var context = CollisionContext.empty();
        //init in loop below
        var entityShape = load(shared, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);

        int minX = Mth.floor(box.minX - 1.0E-7D) - 1;
        int minY = Mth.floor(box.minY - 1.0E-7D) - 1;
        int minZ = Mth.floor(box.minZ - 1.0E-7D) - 1;
        int maxX = Mth.floor(box.maxX + 1.0E-7D) + 1;
        int maxY = Mth.floor(box.maxY + 1.0E-7D) + 1;
        int maxZ = Mth.floor(box.maxZ + 1.0E-7D) + 1;

        BlockGetter[] cachedBlockGetter = new BlockGetter[1];
        long[] cachedBlockGetterPos = new long[1];
        BLOCK_STATE_CACHE.pushRead();
        for (int z = minZ; z <= maxZ; z++) {
            int tz = (z == minZ || z == maxZ) ? 1 : 0;
            for (int y = minY; y <= maxY; y++) {
                int tyz = tz + ((y == minY || y == maxY) ? 1 : 0);
                for (int x = minX; x <= maxX; x++) {
                    int cursorFaceType = tyz + ((x == minX || x == maxX) ? 1 : 0);
                    if (cursorFaceType == Cursor3D.TYPE_CORNER) {
                        continue;
                    }

                    shared.sharedPos.set(x, y, z);
                    var blockstate = BLOCK_STATE_CACHE.getOrCompute(x, y, z, () -> {
                        BlockGetter blockgetter;
                        int chunkX = SectionPos.blockToSectionCoord(shared.sharedPos.getX());
                        int chunkZ = SectionPos.blockToSectionCoord(shared.sharedPos.getZ());
                        long chunkPos = ChunkPos.pack(chunkX, chunkZ);
                        if (cachedBlockGetter[0] != null && cachedBlockGetterPos[0] == chunkPos) {
                            blockgetter = cachedBlockGetter[0];
                        } else {
                            BlockGetter b = level.getChunkForCollisions(chunkX, chunkZ);
                            cachedBlockGetter[0] = b;
                            cachedBlockGetterPos[0] = chunkPos;
                            blockgetter = b;
                        }
                        if (blockgetter == null) {
                            return null;
                        }
                        return blockgetter.getBlockState(shared.sharedPos);
                    });

                    if (blockstate == null || (cursorFaceType == Cursor3D.TYPE_FACE && !blockstate.hasLargeCollisionShape()) || (cursorFaceType == Cursor3D.TYPE_EDGE && !blockstate.is(Blocks.MOVING_PISTON))) {
                        continue;
                    }

                    VoxelShape voxelshape = blockstate.getCollisionShape(level, shared.sharedPos, context);
                    if (voxelshape == Shapes.block()) {
                        if (box.intersects(x, y, z, (double) x + 1.0, (double) y + 1.0, (double) z + 1.0)) {
                            shared.shapesList.add(voxelshape.move(x, y, z));
                        }
                    } else {
                        VoxelShape voxelShape = voxelshape.move(x, y, z);
                        if (!voxelShape.isEmpty() && Shapes.joinIsNotEmpty(voxelShape, entityShape, BooleanOp.AND)) {
                            shared.shapesList.add(voxelShape);
                        }
                    }
                }
            }
        }
        BLOCK_STATE_CACHE.popRead();
    }

    /**
     * @see Entity#collideWithShapes(Vec3, AABB, List)
     */
    private static @Nullable Vec3 collideWithShapes(double dx, double dy, double dz, @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming") AABB entityBB, List<VoxelShape> shapes) {
        if (shapes.isEmpty()) {
            return null;
        } else {
            double d0 = dx;
            double d1 = dy;
            double d2 = dz;
            if (d1 != 0.0D) {
                d1 = Shapes.collide(Direction.Axis.Y, entityBB, shapes, d1);
                if (d1 != 0.0D) {
                    //entityBB = entityBB.move(0.0, d1, 0.0);
                    AABBHelper.move(entityBB, 0, d1, 0);
                }
            }

            boolean flag = Math.abs(d0) < Math.abs(d2);
            if (flag && d2 != 0.0D) {
                d2 = Shapes.collide(Direction.Axis.Z, entityBB, shapes, d2);
                if (d2 != 0.0D) {
                    //entityBB = entityBB.move(0, 0, d2);
                    AABBHelper.move(entityBB, 0, 0, d2);
                }
            }

            if (d0 != 0.0D) {
                d0 = Shapes.collide(Direction.Axis.X, entityBB, shapes, d0);
                if (!flag && d0 != 0.0D) {
                    //entityBB = entityBB.move(d0, 0, 0);
                    AABBHelper.move(entityBB, d0, 0, 0);
                }
            }

            if (!flag && d2 != 0.0D) {
                d2 = Shapes.collide(Direction.Axis.Z, entityBB, shapes, d2);
            }

            return new Vec3(d0, d1, d2);
        }
    }

    public static Cursor3D setCursor3D(Cursor3D thiz, int pOriginX, int pOriginY, int pOriginZ, int pEndX, int pEndY, int pEndZ) {
        thiz.originX = pOriginX;
        thiz.originY = pOriginY;
        thiz.originZ = pOriginZ;
        thiz.width = pEndX - pOriginX + 1;
        thiz.height = pEndY - pOriginY + 1;
        thiz.depth = pEndZ - pOriginZ + 1;
        thiz.end = thiz.width * thiz.height * thiz.depth;
        thiz.index = thiz.x = thiz.y = thiz.z = 0;
        return thiz;
    }

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
    private static VoxelShape load(Shared shared, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
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
                return setVoxelShape(shared.shape, minX, maxX, minY, maxY, minZ, maxZ);
            }
        } else {
            return Shapes.empty();
        }
    }
}
