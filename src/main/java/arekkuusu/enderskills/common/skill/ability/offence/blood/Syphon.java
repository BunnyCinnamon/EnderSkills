package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.Optional;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class Syphon extends BaseAbility implements IImpact {

    public Syphon() {
        super(LibNames.SYPHON, new Properties());
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
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double heal = DSLDefaults.getHeal(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "heal", heal);
        SkillData data = SkillData.of(this)
                .with(INSTANT)
                .put(compound)
                .create();
        EntityThrowableData.throwForTarget(owner, distance, data, false);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.SYPHON);
    }

    @Override
    public void begin(EntityLivingBase target, SkillData data) {
        if (isClientWorld(target)) return;
        Optional.ofNullable(SkillHelper.getOwner(data)).ifPresent(owner -> {
            float heal = (float) NBTHelper.getDouble(data.nbt, "heal");
            float healed = target.getMaxHealth() * heal;
            //Calculate Heal
            float healthBeforeDamage = target.getHealth();
            float lifeTaken = healed;
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
            source.setMagicDamage();
            target.attackEntityFrom(source, lifeTaken);
            float healthAfterDamage = target.getHealth();
            float realLifeTaken = healthBeforeDamage - healthAfterDamage;
            float effectiveFor = realLifeTaken / lifeTaken;
            //Heal
            owner.heal(owner.getMaxHealth() * (heal * effectiveFor));
        });
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ENEMY.apply(owner))) {
            super.apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);
        }
    }
    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLOOD_OFFENCE_CONFIG + LibNames.SYPHON;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
