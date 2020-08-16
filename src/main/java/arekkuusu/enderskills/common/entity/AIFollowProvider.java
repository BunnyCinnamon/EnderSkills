package arekkuusu.enderskills.common.entity;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class AIFollowProvider extends EntityAIBase {

    public final EntityLiving entity;
    public Supplier<EntityLivingBase> ownerSupplier;
    public EntityLivingBase owner;
    public final double followSpeed;
    public final PathNavigate petPathfinder;
    public int timeToRecalcPath;
    public float maxDist;
    public float minDist;
    public float oldWaterCost;
    public World world;

    public AIFollowProvider(EntityLiving entity, Supplier<EntityLivingBase> ownerSupplier, double followSpeedIn, float minDistIn, float maxDistIn) {
        this.entity = entity;
        this.ownerSupplier = ownerSupplier;
        this.world = entity.world;
        this.followSpeed = followSpeedIn;
        this.petPathfinder = entity.getNavigator();
        this.minDist = minDistIn;
        this.maxDist = maxDistIn;
        this.setMutexBits(3);
    }

    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.ownerSupplier.get();
        if (entitylivingbase == null) {
            return false;
        } else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer) entitylivingbase).isSpectator()) {
            return false;
        } else if (this.entity.getDistanceSq(entitylivingbase) < (double) (this.minDist * this.minDist)) {
            return false;
        } else {
            this.owner = entitylivingbase;
            return true;
        }
    }

    public boolean shouldContinueExecuting() {
        return !this.petPathfinder.noPath() && this.entity.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
    }

    public void startExecuting() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.entity.getPathPriority(PathNodeType.WATER);
        this.entity.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    public void resetTask() {
        this.owner = null;
        this.petPathfinder.clearPath();
        this.entity.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    public void updateTask() {
        this.entity.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.entity.getVerticalFaceSpeed());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;

            if (!this.petPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed)) {
                if (!this.entity.getLeashed() && !this.entity.isRiding()) {
                    if (this.entity.getDistanceSq(this.owner) >= 144.0D) {
                        int i = MathHelper.floor(this.owner.posX) - 2;
                        int j = MathHelper.floor(this.owner.posZ) - 2;
                        int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);

                        for (int l = 0; l <= 4; ++l) {
                            for (int i1 = 0; i1 <= 4; ++i1) {
                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
                                    this.entity.setLocationAndAngles((double) ((float) (i + l) + 0.5F), (double) k, (double) ((float) (j + i1) + 0.5F), this.entity.rotationYaw, this.entity.rotationPitch);
                                    this.petPathfinder.clearPath();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_) {
        BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
        IBlockState iblockstate = this.world.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(this.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(this.entity) && this.world.isAirBlock(blockpos.up()) && this.world.isAirBlock(blockpos.up(2));
    }
}
