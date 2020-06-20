package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SkillHelper {

    public static boolean isSkillDamage(DamageSource source) {
        return source.getDamageType().matches(BaseAbility.DAMAGE_HIT_TYPE + "|" + BaseAbility.DAMAGE_DOT_TYPE);
    }

    public static void getActiveOwner(Entity entity, Skill skill, Consumer<SkillHolder> consumer) {
        SkillHelper.getActive(entity, skill, holder -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                if (entity == user) {
                    consumer.accept(holder);
                }
            });
        });
    }

    public static void getActiveNotOwner(Entity entity, Skill skill, Consumer<SkillHolder> consumer) {
        SkillHelper.getActive(entity, skill, holder -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                if (entity != user) {
                    consumer.accept(holder);
                }
            });
        });
    }

    public static void getActive(Entity entity, Skill skill, Consumer<SkillHolder> consumer) {
        Capabilities.get(entity).ifPresent(c -> {
            c.getActives().stream().filter(h -> h.data.skill == skill).forEach(consumer);
        });
    }

    public static boolean isActiveOwner(Entity entity, Skill skill) {
        return SkillHelper.isActive(entity, skill, h -> Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, h.data.nbt, "user")).map(e -> e == entity).orElse(false));
    }

    public static boolean isActiveNotOwner(Entity entity, Skill skill) {
        return SkillHelper.isActive(entity, skill, h -> Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, h.data.nbt, "user")).map(e -> e != entity).orElse(false));
    }

    public static boolean isActive(Entity entity, Skill skill, Function<SkillHolder, Boolean> function) {
       return Capabilities.get(entity).map(c -> c.getActives().stream().filter(h -> h.data.skill == skill).anyMatch(function::apply)).orElse(false);
    }
}
