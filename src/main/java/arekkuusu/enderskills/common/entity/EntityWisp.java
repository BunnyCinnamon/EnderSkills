package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;

import java.util.List;

public class EntityWisp extends EntityThrowableData {

    public int tickDelay;

    public EntityWisp(World worldIn) {
        super(worldIn);
    }

    public EntityWisp(World worldIn, EntityLivingBase owner, double distance, SkillData data, boolean gravity) {
        super(worldIn, owner, distance, data, gravity);
        this.rotationYaw = owner.rotationYaw;
        this.rotationPitch = owner.rotationPitch;
    }

    @Override
    public void onUpdate() {
        if (getFollowId() != null) {
            if(!world.isRemote) {
                if(rand.nextDouble() < 0.6D) {
                    this.motionX += (rand.nextDouble() * 2 - 1) * 0.25D;
                    this.motionY += (rand.nextDouble() * 2 - 1) * 0.25D;
                    this.motionZ += (rand.nextDouble() * 2 - 1) * 0.25D;
                }
            }
            super.onUpdate();
        } else {
            if(!world.isRemote) {
                if (tickDelay > getData().nbt.getInteger("delay")) {
                    EntityLivingBase owner = getEntityByUUID(getOwnerId());
                    if(owner != null && getFollowId() == null) {
                        List<Entity> list = RayTraceHelper.getEntitiesInCone2(this, startVector, getDistance(), 60, TeamHelper.getEnemyTeamPredicate(owner));
                        if (!list.isEmpty()) {
                            Entity target = list.get(0);
                            for (Entity entity : list) {
                                if (entity.getDistance(owner) < target.getDistance(owner)) {
                                    target = entity;
                                }
                            }
                            if(target instanceof EntityLivingBase) setFollowId(target.getUniqueID());
                        }
                    }
                    this.motionX = 0;
                    this.motionY = 0;
                    this.motionZ = 0;
                    if(tickDelay > getData().nbt.getInteger("delay") + 600) setDead();
                } else {
                    move(MoverType.SELF, motionX, motionY, motionZ);
                    markVelocityChanged();
                }
            }
            this.tickDelay++;
        }
    }
}
