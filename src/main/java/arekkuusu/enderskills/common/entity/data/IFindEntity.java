package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public interface IFindEntity extends IScanEntities {
    void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData);
}
