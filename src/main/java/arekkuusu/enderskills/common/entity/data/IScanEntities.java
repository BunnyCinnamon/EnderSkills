package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.TeamHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;
import java.util.List;

public interface IScanEntities {
    default List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        return source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
    }

    default void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        //For-Rent
    }
}
