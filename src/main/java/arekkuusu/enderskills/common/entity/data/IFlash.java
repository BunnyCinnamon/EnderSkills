package arekkuusu.enderskills.common.entity.data;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public interface IFlash {
    void onFlash(Entity source, @Nullable EntityLivingBase owner, SkillData skillData);
}
