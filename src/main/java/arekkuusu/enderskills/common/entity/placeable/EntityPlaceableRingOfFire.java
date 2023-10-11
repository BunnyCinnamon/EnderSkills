package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityPlaceableRingOfFire extends EntityPlaceableFloor {

    public EntityPlaceableRingOfFire(World world) {
        super(world);
    }

    public EntityPlaceableRingOfFire(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(world.isRemote) {
            List<BlockPos> terrain = this.getTerrainBlocks();
            for (BlockPos targetBlockPos : terrain) {
                for (int i = 0; i < 4; i++) {
                    if (this.world.rand.nextDouble() < 0.2D && ClientProxy.canParticleSpawn()) {
                        boolean isBorder = false;
                        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                            BlockPos nextToTarget = targetBlockPos.offset(facing);
                            BlockPos perpendicularToTarget = nextToTarget.offset(facing.rotateY());
                            boolean isCloseToNonTerrain = terrain.stream().noneMatch(pos -> pos.equals(nextToTarget)
                                    || pos.down().equals(nextToTarget)
                                    || pos.up().equals(nextToTarget)
                                    || pos.equals(perpendicularToTarget)
                                    || pos.down().equals(perpendicularToTarget)
                                    || pos.up().equals(perpendicularToTarget)
                            );
                            if (isCloseToNonTerrain) {
                                isBorder = true;
                                break;
                            }
                        }

                        double posX = targetBlockPos.getX() + 1 * this.world.rand.nextDouble();
                        double posY = targetBlockPos.getY() + 1D + 0.1 * this.world.rand.nextDouble();
                        double posZ = targetBlockPos.getZ() + 1 * this.world.rand.nextDouble();
                        double motionX = (this.world.rand.nextDouble() - 0.5D) * 0.25D;
                        double motionZ = (this.world.rand.nextDouble() - 0.5D) * 0.25D;
                        if (isBorder) {
                            EnderSkills.getProxy().spawnParticleLuminescence(this.world, new Vec3d(posX, posY, posZ), new Vec3d(motionX, 0.05D, motionZ), 4F, 25, ResourceLibrary.GLOW, 0xFFE077);
                        }
                        if(ClientProxy.canParticleSpawn()) {
                            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, motionX, 0.01D, motionZ);
                        }
                    }
                }
            }
        }
    }
}
