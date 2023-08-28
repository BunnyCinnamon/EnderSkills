package arekkuusu.enderskills.api.configuration;

import arekkuusu.enderskills.api.event.SkillDurationEvent;
import arekkuusu.enderskills.api.event.SkillRangeEvent;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.skill.ability.defense.electric.ElectricPulse;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public final class DSLDefaults {

    public static int getEndurance(Skill skill, int lvl) {
        return DSLEvaluator.evaluateInt(skill, "ENDURANCE", lvl, 1D);
    }

    public static float getModifier(Skill skill, int lvl) {
        return DSLEvaluator.evaluateFloat(skill, "MODIFIER", lvl, 1D);
    }

    public static float getDelay(Skill skill, int lvl) {
        return DSLEvaluator.evaluateFloat(skill, "DELAY", lvl, 1D);
    }

    public static float getRegen(Skill skill, int lvl) {
        return DSLEvaluator.evaluateFloat(skill, "REGEN", lvl, 1D);
    }

    public static float getExperience(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "XP", level, CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public static float getDamage(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "DAMAGE", level, CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public static float getDamageMimicry(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "DAMAGE_MIRROR", level, CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public static float getDamageOverTime(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "DOT", level, CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public static float getHealth(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "HEALTH", level, CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
    }

    public static int getStun(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "STUN", level, 1D);
    }

    public static int getCooldown(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "COOLDOWN", level, 1D);
    }

    public static int getDuration(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "DURATION", level, 1D);
    }

    public static int getDamageDuration(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "DOT_DURATION", level, 1D);
    }

    public static int getIntervalDuration(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "INTERVAL", level, 1D);
    }

    public static float getForce(Skill skill, int level) {
        return DSLEvaluator.evaluateFloat(skill, "FORCE", level, 1D);
    }

    public static int getHeight(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "HEIGHT", level, 1D);
    }

    public static int getWidth(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "WIDTH", level, 1D);
    }

    public static int getRange(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "RANGE", level, 1D);
    }

    public static int getRangeExtension(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "RANGE_EXTRA", level, 1D);
    }

    public static double getSlow(Skill skill, int level) {
        return DSLEvaluator.evaluateDouble(skill, "SLOW", level, 1D);
    }

    public static double getPower(Skill skill, int level) {
        return DSLEvaluator.evaluateDouble(skill, "POWER", level, CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
    }

    public static double getSize(Skill skill, int level) {
        return DSLEvaluator.evaluateDouble(skill, "SIZE", level, 1D);
    }

    public static double getHeal(Skill skill, int level) {
        return DSLEvaluator.evaluateDouble(skill, "HEAL", level, CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
    }

    public static int getHealDuration(Skill skill, int level) {
        return DSLEvaluator.evaluateInt(skill, "HEAL_DURATION", level, 1D);
    }

    public static SkillDurationEvent triggerDuration(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getDuration(skill, level);
        return SkillDurationEvent.trigger(entity, skill, original);
    }

    public static SkillDurationEvent triggerDamageDuration(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getDamageDuration(skill, level);
        return SkillDurationEvent.trigger(entity, skill, original);
    }

    public static SkillDurationEvent triggerIntervalDuration(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getIntervalDuration(skill, level);
        return SkillDurationEvent.trigger(entity, skill, original);
    }

    public static SkillDurationEvent triggerHealDuration(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getHealDuration(skill, level);
        return SkillDurationEvent.trigger(entity, skill, original);
    }

    public static SkillRangeEvent triggerRange(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getRange(skill, level);
        return SkillRangeEvent.trigger(entity, skill, original);
    }

    public static SkillRangeEvent triggerRangeExtension(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getRangeExtension(skill, level);
        return SkillRangeEvent.trigger(entity, skill, original);
    }

    public static SkillRangeEvent triggerHeight(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getHeight(skill, level);
        return SkillRangeEvent.trigger(entity, skill, original);
    }

    public static SkillRangeEvent triggerWidth(@Nullable EntityLivingBase entity, Skill skill, int level) {
        int original = DSLDefaults.getWidth(skill, level);
        return SkillRangeEvent.trigger(entity, skill, original);
    }

    public static SkillRangeEvent triggerSize(@Nullable EntityLivingBase entity, Skill skill, int level) {
        double original = DSLDefaults.getSize(skill, level);
        return SkillRangeEvent.trigger(entity, skill, original);
    }
}
