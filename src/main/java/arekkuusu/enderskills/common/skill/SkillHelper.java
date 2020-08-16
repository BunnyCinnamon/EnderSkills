package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SkillHelper {

    public static boolean isSkillDamage(DamageSource source) {
        return source.getDamageType().matches(BaseAbility.DAMAGE_HIT_TYPE + "|" + BaseAbility.DAMAGE_DOT_TYPE);
    }

    public static boolean isOwner(Entity entity, SkillData data) {
        return data.id.equals(entity.getUniqueID().toString());
    }

    public static boolean isActive(Entity entity, Skill skill) {
        return Capabilities.get(entity).map(c -> c.isActive(skill)).orElse(false);
    }

    public static boolean isActiveFrom(Entity entity, Skill skill) {
        return SkillHelper.isActive(entity, skill, entity.getUniqueID().toString());
    }

    public static boolean isActive(Entity entity, Skill skill, String id) {
        return Capabilities.get(entity).map(c -> c.getActives().stream().anyMatch(h -> h.data.skill == skill && !h.isDead() && h.data.id.equals(id))).orElse(false);
    }

    public static Optional<SkillData> getActiveFrom(Entity owner, Skill skill) {
        return SkillHelper.getActive(owner, skill, owner.getUniqueID().toString());
    }

    public static Optional<SkillData> getActive(Entity owner, Skill skill, String id) {
        return Capabilities.get(owner).flatMap(c -> c.getActives().stream().filter(h -> h.data.skill == skill && !h.isDead() && h.data.id.equals(id)).map(h -> h.data).findFirst());
    }

    @Nullable
    public static EntityLivingBase getOwner(SkillData data) {
        return NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "owner");
    }
}
