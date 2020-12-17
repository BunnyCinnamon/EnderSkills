package arekkuusu.enderskills.api.helper;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("Guava")
public final class RayTraceHelper {

    public static boolean isBlockTrace(RayTraceResult result) {
        return result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    public static boolean isEntityTrace(@Nullable RayTraceResult result, Predicate<Entity> predicate) {
        return result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLivingBase && predicate.test(result.entityHit);
    }

    public static List<Entity> getEntitiesInCone(Entity source, double distance, double degrees, Predicate<Entity> predicate) {
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );
        AxisAlignedBB bb = new AxisAlignedBB(
                targetVector.x - distance, targetVector.y - distance, targetVector.z - distance,
                targetVector.x + distance, targetVector.y + distance, targetVector.z + distance
        );
        List<Entity> entities = Lists.newArrayList();
        for (Entity entity : source.world.getEntitiesInAABBexcluding(source, bb, predicate)) {
            if (isTargetCone(entity, source, degrees)) {
                entities.add(entity);
            }
        }
        return entities;
    }

    public static boolean isTargetCone(Entity source, Entity target, double fov) {
        Vec3d positionTarget = target.getPositionEyes(1F);
        Vec3d lookTarget = target.getLookVec().normalize();
        Vec3d positionAttacker = source.getPositionEyes(1F);

        Vec3d origin = new Vec3d(0, 0, 0);
        Vec3d pointA = lookTarget.add(positionTarget).subtract(positionTarget);
        Vec3d pointB = positionAttacker.subtract(positionTarget);
        double pointADistance = pointA.distanceTo(pointB);
        double pointBDistance = pointB.distanceTo(origin);

        if (pointADistance < pointBDistance) {
            double ab = (pointA.x * pointB.x) + (pointA.y * pointB.y) + (pointA.z * pointB.z);
            double a = Math.sqrt(Math.pow(pointA.x, 2D) + Math.pow(pointA.y, 2D) + Math.pow(pointA.z, 2D));
            double b = Math.sqrt(Math.pow(pointB.x, 2D) + Math.pow(pointB.y, 2D) + Math.pow(pointB.z, 2D));
            double angle = Math.acos(ab / (a * b)) * (180 / Math.PI);
            return angle > -fov && angle < fov;
        }
        return false;
    }

    public static Optional<Entity> getEntityLookedAt(Entity source, double distance, Predicate<Entity> predicate) {
        World world = source.getEntityWorld();
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );

        RayTraceResult traceBlocks = rayTraceBlocks(world, eyesVector, targetVector);
        if (traceBlocks != null) {
            distance = traceBlocks.hitVec.distanceTo(eyesVector);
            targetVector = eyesVector.addVector(lookVector.x * distance, lookVector.y * distance, lookVector.z * distance);
        }
        return Optional.ofNullable(RayTraceHelper.rayTraceEntitiesExcept(world, eyesVector, targetVector, Predicates.and(e -> e != source, predicate))).map(ray -> ray.entityHit);
    }

    public static Optional<Vec3d> getVecLookedAt(Entity source, double distance) {
        World world = source.getEntityWorld();
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );

        RayTraceResult traceBlocks = rayTraceBlocks(world, eyesVector, targetVector);
        return Optional.of(traceBlocks != null ? traceBlocks.hitVec : targetVector);
    }

    public static Optional<BlockPos> getPosLookedAt(Entity source, double distance) {
        World world = source.getEntityWorld();
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );

        RayTraceResult traceBlocks = rayTraceBlocks(world, eyesVector, targetVector);
        return Optional.ofNullable(traceBlocks != null && traceBlocks.typeOfHit == RayTraceResult.Type.BLOCK ? traceBlocks.getBlockPos() : null);
    }

    public static Optional<Entity> getEntityLookedAt(Entity source, double distance) {
        World world = source.getEntityWorld();
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distance,
                lookVector.y * distance,
                lookVector.z * distance
        );

        RayTraceResult traceBlocks = rayTraceEntitiesExcept(world, eyesVector, targetVector, e -> e != source);
        return Optional.ofNullable(traceBlocks != null && traceBlocks.typeOfHit == RayTraceResult.Type.ENTITY ? traceBlocks.entityHit : null);
    }

    public static Optional<BlockPos> getFloorLookedAt(Entity source, double distanceForward, double distanceDown) {
        World world = source.getEntityWorld();
        Vec3d eyesVector = source.getPositionEyes(1F);
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = eyesVector.addVector(
                lookVector.x * distanceForward,
                lookVector.y * distanceForward,
                lookVector.z * distanceForward
        );

        RayTraceResult traceBlocks = rayTraceBlocks(world, eyesVector, targetVector);
        if (traceBlocks == null || traceBlocks.typeOfHit != RayTraceResult.Type.BLOCK) {
            Vec3d vec = traceBlocks != null ? traceBlocks.hitVec : targetVector;
            BlockPos pos = new BlockPos(vec);
            IBlockState state = world.getBlockState(pos);
            int i = 0;
            while (i++ < ((int) distanceDown + (int) Math.ceil(source.height)) && state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB) {
                pos = pos.down();
                vec = vec.subtract(0, 1, 0);
                state = world.getBlockState(pos);
            }
            if (state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB) {
                traceBlocks = new RayTraceResult(vec, EnumFacing.UP, pos);
            }
        }
        return Optional.ofNullable(traceBlocks != null && traceBlocks.typeOfHit == RayTraceResult.Type.BLOCK ? traceBlocks.getBlockPos() : null);
    }

    public static RayTraceResult forwardsRaycast(Entity projectile, boolean includeEntities, boolean ignoreExcludedEntity, Entity excludedEntity) {
        double d0 = projectile.posX;
        double d1 = projectile.posY;
        double d2 = projectile.posZ;
        double d3 = projectile.motionX;
        double d4 = projectile.motionY;
        double d5 = projectile.motionZ;
        World world = projectile.world;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        Vec3d vec3d1 = new Vec3d(d0 + d3, d1 + d4, d2 + d5);
        RayTraceResult raytraceresult = world.rayTraceBlocks(vec3d, vec3d1, false, true, false);

        if (includeEntities) {
            if (raytraceresult != null) {
                vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
            }

            Entity entity = null;
            List<Entity> list = world.getEntitiesInAABBexcluding(projectile, projectile.getEntityBoundingBox().expand(d3, d4, d5).grow(1.0D), Predicates.and(TeamHelper.NOT_CREATIVE, e -> e != excludedEntity));
            double d6 = 0.0D;

            for (Entity entity1 : list) {
                if (entity1.canBeCollidedWith() && (ignoreExcludedEntity || !entity1.isEntityEqual(excludedEntity)) && !entity1.noClip) {
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
                    RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

                    if (raytraceresult1 != null) {
                        double d7 = vec3d.squareDistanceTo(raytraceresult1.hitVec);

                        if (d7 < d6 || d6 == 0.0D) {
                            entity = entity1;
                            d6 = d7;
                        }
                    }
                }
            }

            if (entity != null) {
                raytraceresult = new RayTraceResult(entity);
            }
        }

        return raytraceresult;
    }

    @Nullable
    public static RayTraceResult rayTraceEntitiesExcept(World world, Vec3d vecFrom, Vec3d vecTo, Predicate<Entity> predicate) {
        if (!Double.isNaN(vecFrom.x) && !Double.isNaN(vecFrom.y) && !Double.isNaN(vecFrom.z)) {
            if (!Double.isNaN(vecTo.x) && !Double.isNaN(vecTo.y) && !Double.isNaN(vecTo.z)) {
                Vec3d difference = vecTo.subtract(vecFrom);
                double distance = vecFrom.distanceTo(vecTo);
                AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(vecFrom))
                        .expand(difference.x * distance, difference.y * distance, difference.z * distance)
                        .grow(1.0D, 1.0D, 1.0D);
                List<Entity> list = world.getEntitiesWithinAABB(Entity.class, bb, predicate);
                Entity hitEntity = null;
                Vec3d hitVec = null;
                double minDistance = distance;
                for (Entity entity : list) {
                    AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
                    RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vecFrom, vecTo);

                    if (axisalignedbb.contains(vecFrom)) {
                        if (minDistance >= 0.0D) {
                            hitEntity = entity;
                            hitVec = raytraceresult == null ? vecFrom : raytraceresult.hitVec;
                            minDistance = 0.0D;
                        }
                    } else if (raytraceresult != null) {
                        double distanceToEntity = vecFrom.distanceTo(raytraceresult.hitVec);

                        if (distanceToEntity < minDistance || minDistance == 0.0D) {
                            hitEntity = entity;
                            hitVec = raytraceresult.hitVec;
                        } else {
                            hitEntity = entity;
                            hitVec = raytraceresult.hitVec;
                            minDistance = distanceToEntity;
                        }
                    }
                }
                if (hitEntity != null) {
                    return new RayTraceResult(hitEntity, hitVec);
                }
                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static RayTraceResult rayTraceBlocks(World world, Vec3d vecFrom, Vec3d vecTo) {
        if (!Double.isNaN(vecFrom.x) && !Double.isNaN(vecFrom.y) && !Double.isNaN(vecFrom.z)) {
            if (!Double.isNaN(vecTo.x) && !Double.isNaN(vecTo.y) && !Double.isNaN(vecTo.z)) {
                int i = MathHelper.floor(vecTo.x);
                int j = MathHelper.floor(vecTo.y);
                int k = MathHelper.floor(vecTo.z);
                int l = MathHelper.floor(vecFrom.x);
                int i1 = MathHelper.floor(vecFrom.y);
                int j1 = MathHelper.floor(vecFrom.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, false)) {
                    return iblockstate.collisionRayTrace(world, blockpos, vecFrom, vecTo);
                }

                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vecFrom.x) || Double.isNaN(vecFrom.y) || Double.isNaN(vecFrom.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vecTo.x - vecFrom.x;
                    double d7 = vecTo.y - vecFrom.y;
                    double d8 = vecTo.z - vecFrom.z;

                    if (flag2) {
                        d3 = (d0 - vecFrom.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vecFrom.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vecFrom.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vecFrom = new Vec3d(d0, vecFrom.y + d7 * d3, vecFrom.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vecFrom = new Vec3d(vecFrom.x + d6 * d4, d1, vecFrom.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vecFrom = new Vec3d(vecFrom.x + d6 * d5, vecFrom.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vecFrom.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vecFrom.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vecFrom.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState state = world.getBlockState(blockpos);
                    Block block1 = state.getBlock();

                    if (state.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && block1.canCollideCheck(state, false)) {
                        RayTraceResult temp = state.collisionRayTrace(world, blockpos, vecFrom, vecTo);
                        if (temp != null || state.getCollisionBoundingBox(world, blockpos) == Block.FULL_BLOCK_AABB) {
                            return temp;
                        }
                    }
                }
                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
