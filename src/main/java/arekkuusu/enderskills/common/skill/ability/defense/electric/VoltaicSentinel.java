package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.EntityVoltaicSentinel;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;

public class VoltaicSentinel extends BaseAbility {

    public VoltaicSentinel() {
        super(LibNames.VOLTAIC_SENTINEL, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (hasNoCooldown(skillInfo) && canActivate(owner)) {
                InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                int level = infoUpgradeable.getLevel();
                if (infoCooldown.canSetCooldown(owner)) {
                    infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                }

                //
                float health = DSLDefaults.getHealth(this, level);
                float damage = DSLDefaults.getDamage(this, level);
                EntityVoltaicSentinel sentinel = new EntityVoltaicSentinel(owner.world);
                sentinel.setPosition(owner.posX, owner.posY + owner.height + 0.1D, owner.posZ);
                sentinel.setOwnerId(owner.getUniqueID());
                sentinel.setFollowId(owner.getUniqueID());
                sentinel.setMaxHealth(health);
                sentinel.setHealth(health);
                sentinel.setDamage(damage);
                sentinel.spawnEntity();

                int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setEntity(compound, sentinel, "sentinel");
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(time)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);

                SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.VOLTAIC_SENTINEL_SUMMON);
            }
        } else {
            SkillHelper.getActiveEntityFrom(owner, this, EntityVoltaicSentinel.class, "sentinel").ifPresent(e -> {
                Entity found = RayTraceHelper.getEntityLookedAt(owner, 5).orElse(null);
                if (!(found instanceof EntityLivingBase) || !TeamHelper.SELECTOR_ALLY.apply(owner).test(found))
                    found = owner;
                e.setFollowId(found.getUniqueID());
                e.teleportTo((EntityLivingBase) found);
            });
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (NBTHelper.getEntity(EntityVoltaicSentinel.class, data.nbt, "sentinel") == null) {
            super.unapply(owner, data);
            super.async(owner, data);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.VOLTAIC_SENTINEL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
