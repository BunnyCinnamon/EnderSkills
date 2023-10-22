package arekkuusu.enderskills.common.skill.ability.offence.blackflame;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableFloatCustom;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.effect.BlackFlame;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class BlackVolleyFlame extends BaseAbility implements IExpand, IImpact {

    public BlackVolleyFlame() {
        super(LibNames.BLACK_VOLLEY_FLAME, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();

        if (infoCooldown.canSetCooldown(owner)) {
            infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
        }

        //
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        int amount = DSLDefaults.getAmount(this, level);
        int intervalDuration = DSLDefaults.getIntervalDuration(this, level);
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        double true_damage = DSLDefaults.getTrueDamage(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "true_damage", true_damage);
        NBTHelper.setInteger(compound, "amount", amount);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setInteger(compound, "intervalDuration", intervalDuration);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        super.apply(owner, data);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.FLAMING_BREATH);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (!source.world.isRemote && trace.entityHit instanceof EntityLivingBase) {
            double damage = skillData.nbt.getDouble("damage");
            double true_damage = skillData.nbt.getDouble("true_damage");
            BlackFlame.dealDamage(this, (EntityLivingBase) trace.entityHit, owner, damage);
            BlackFlame.dealTrueDamageHAHAHA(this, (EntityLivingBase) trace.entityHit, owner, true_damage);

            SoundHelper.playSound(source.world, trace.entityHit.getPosition(), ModSounds.FIRE_HIT);
        }
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        int intervalDuration = NBTHelper.getInteger(data.nbt, "intervalDuration");
        if (entity.ticksExisted % intervalDuration != 0) return;
        double range = NBTHelper.getDouble(data.nbt, "range");
        int amount = NBTHelper.getInteger(data.nbt, "amount");

        for (int i = 0; i <= amount; i++) {
            EntityThrowableData.throwFor(entity, range, data, 6F, false);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLACK_FLAME_OFFENCE_CONFIG + LibNames.BLACK_VOLLEY_FLAME;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
