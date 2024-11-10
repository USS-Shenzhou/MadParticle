package cn.ussshenzhou.madparticle.util;

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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class MovementHelper {
    private static final ThreadLocal<AABB> SHARED_AABB = ThreadLocal.withInitial(() -> new AABB(0, 0, 0, 0, 0, 0));
    private static final ThreadLocal<BlockPos.MutableBlockPos> SHARED_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<Cursor3D> SHARED_CURSOR3D = ThreadLocal.withInitial(() -> new Cursor3D(0, 0, 0, 0, 0, 0));
    private static final ThreadLocal<LinkedList<VoxelShape>> SHARED_SHAPE_LIST = ThreadLocal.withInitial(LinkedList::new);

    /**
     * @see Entity#collideBoundingBox(Entity, Vec3, AABB, Level, List)
     */
    public static @Nullable Vec3 collideBoundingBox(@Nullable Entity entity, double dx, double dy, double dz, AABB collisionBox, Level level, List<VoxelShape> potentialHits) {
        var collisionBoxShared = AABBHelper.set(SHARED_AABB.get(), collisionBox);
        LinkedList<VoxelShape> list = SHARED_SHAPE_LIST.get();
        list.clear();
        if (!potentialHits.isEmpty()) {
            list.addAll(potentialHits);
        }

        WorldBorder worldborder = level.getWorldBorder();
        boolean flag = entity != null && worldborder.isInsideCloseToBorder(entity, collisionBoxShared.expandTowards(dx, dy, dz));
        if (flag) {
            list.add(worldborder.getCollisionShape());
        }

        getBlockCollisions(list, level, entity, AABBHelper.expandTowards(collisionBoxShared, dx, dy, dz), false);
        AABBHelper.set(collisionBoxShared, collisionBox);
        return collideWithShapes(dx, dy, dz, collisionBoxShared, list);
    }

    /**
     * @see net.minecraft.world.level.BlockCollisions
     */
    public static void getBlockCollisions(List<VoxelShape> list, Level level, @Nullable Entity entity, AABB box, boolean onlySuffocatingBlocks) {
        var context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        //init in loop below
        var pos = SHARED_POS.get();
        var entityShape = VoxelShapeHelper.create(box);
        var cursor = setCursor3D(SHARED_CURSOR3D.get(),
                Mth.floor(box.minX - 1.0E-7D) - 1,
                Mth.floor(box.minY - 1.0E-7D) - 1,
                Mth.floor(box.minZ - 1.0E-7D) - 1,
                Mth.floor(box.maxX + 1.0E-7D) + 1,
                Mth.floor(box.maxY + 1.0E-7D) + 1,
                Mth.floor(box.maxZ + 1.0E-7D) + 1
        );
        BlockGetter cachedBlockGetter = null;
        long cachedBlockGetterPos = 0;
        while (cursor.advance()) {
            int i = cursor.nextX();
            int j = cursor.nextY();
            int k = cursor.nextZ();
            int l = cursor.getNextType();

            if (l != 3) {
                BlockGetter blockgetter;
                int x = SectionPos.blockToSectionCoord(i);
                int z = SectionPos.blockToSectionCoord(k);
                long c = ChunkPos.asLong(i, j);
                if (cachedBlockGetter != null && cachedBlockGetterPos == z) {
                    blockgetter = cachedBlockGetter;
                } else {
                    BlockGetter b = level.getChunkForCollisions(x, z);
                    cachedBlockGetter = b;
                    cachedBlockGetterPos = c;
                    blockgetter = b;
                }

                if (blockgetter != null) {
                    pos.set(i, j, k);
                    BlockState blockstate = blockgetter.getBlockState(pos);

                    if ((!onlySuffocatingBlocks || blockstate.isSuffocating(blockgetter, pos))
                            && (l != 1 || blockstate.hasLargeCollisionShape())
                            && (l != 2 || blockstate.is(Blocks.MOVING_PISTON))) {

                        VoxelShape voxelshape = blockstate.getCollisionShape(level, pos, context);
                        if (voxelshape == Shapes.block()) {
                            if (box.intersects(i, j, k, (double) i + 1.0, (double) j + 1.0, (double) k + 1.0)) {
                                list.add(voxelshape.move(i, j, k));
                            }
                        } else {
                            VoxelShape voxelshape1 = voxelshape.move(i, j, k);
                            if (!voxelshape1.isEmpty() && Shapes.joinIsNotEmpty(voxelshape1, entityShape, BooleanOp.AND)) {
                                list.add(voxelshape1);
                            }
                        }
                    }
                }
            }
        }
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
}
