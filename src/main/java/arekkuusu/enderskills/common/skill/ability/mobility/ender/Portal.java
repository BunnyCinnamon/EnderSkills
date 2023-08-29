package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.entity.EntityPortal;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class Portal extends BaseAbility {

    public Portal() {
        super(LibNames.PORTAL, new Properties());
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
                int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
                EntityPortal portal = new EntityPortal(owner.world, time);
                portal.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
                owner.world.spawnEntity(portal);
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, portal, "portal");
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setInteger(compound, "time", time);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.SAME)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                EntityPortal originalPortal = NBTHelper.getEntity(EntityPortal.class, data.nbt, "portal");
                if (originalPortal != null && !originalPortal.isDead) {
                    if (originalPortal.getTarget() == null) {
                        if (isActionable(owner) && canActivate(owner)) {
                            InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                            InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                            int level = infoUpgradeable.getLevel();
                            if (infoCooldown.canSetCooldown(owner)) {
                                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                            }

                            //
                            EntityPortal portal = new EntityPortal(owner.world, NBTHelper.getInteger(data.nbt, "time"));
                            portal.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
                            owner.world.spawnEntity(portal);
                            originalPortal.setTarget(portal);
                            portal.setTarget(originalPortal);
                            super.sync(owner);
                        }
                    } else {
                        originalPortal.setDead();
                        originalPortal.getTarget().setDead();
                    }
                }
            });
            super.unapply(owner);
            super.async(owner);
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        EntityPortal portal = NBTHelper.getEntity(EntityPortal.class, data.nbt, "portal");
        if (portal == null) {
            Capabilities.get(owner).flatMap(c -> c.getOwned(this)).ifPresent(skillInfo -> {
                InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                int level = infoUpgradeable.getLevel();
                if (infoCooldown.canSetCooldown(owner)) {
                    infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                }
                super.sync(owner);
            });
            super.unapply(owner, data);
            super.async(owner, data);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.PORTAL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
