package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.skill.SkillHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityPlaceableExplode extends EntityPlaceableData {

    public EntityPlaceableExplode(World world) {
        super(world);
    }

    public EntityPlaceableExplode(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    public void onUpdate() {
        this.onEntityUpdate();
        SkillData data = getData();
        EntityLivingBase owner = SkillHelper.getOwner(data);
        if (!world.isRemote) {
            if (this.tick == MIN_TIME) {
                if (data.skill instanceof IScanEntities) {
                    List<Entity> entities = ((IScanEntities) data.skill).getScan(this, owner, data.copy(), getRadius() * ((float) tick / (float) getLifeTime()));
                    if (!entities.isEmpty()) {
                        for (Entity entity : entities) {
                            if (entity instanceof EntityLivingBase) {
                                if (affectedEntities.add(entity)) {
                                    if (data.skill instanceof IFindEntity) {
                                        ((IFindEntity) data.skill).onFound(this, owner, (EntityLivingBase) entity, data.copy());
                                    }
                                }
                                if (data.skill instanceof IScanEntities) {
                                    ((IScanEntities) data.skill).onScan(this, owner, (EntityLivingBase) entity, data.copy());
                                }
                            }
                        }
                    }
                }
            } else if (tick >= getLifeTime()) {
                setDead();
            }
        }
        if (tick == MIN_TIME) {
            if (!world.isRemote) {
                List<EntityLivingBase> fucc = getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
                for (EntityLivingBase entity : fucc) {
                    MotionHelper.pushAround(this, entity, 2);
                }
            }
            if (world.isRemote) {
                double scale = getRadius() * 2;
                for (int i = 0; i < 18; i++) {
                    if (world.rand.nextDouble() < 0.8D) {
                        Vec3d vec = getPositionVector();
                        double posX = vec.x + scale * (world.rand.nextDouble() - 0.5);
                        double posY = vec.y + scale * (world.rand.nextDouble() - 0.5);
                        double posZ = vec.z + scale * (world.rand.nextDouble() - 0.5);
                        world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
        if (MIN_TIME > this.tick && data.skill instanceof IExpand) {
            setEntityBoundingBox(((IExpand) data.skill).expand(this, getEntityBoundingBox(), getRadius() / (float) MIN_TIME));
        }
        this.tick++;
    }
}
