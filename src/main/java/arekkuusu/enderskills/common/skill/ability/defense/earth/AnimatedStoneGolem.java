package arekkuusu.enderskills.common.skill.ability.defense.earth;

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
import arekkuusu.enderskills.common.entity.EntityStoneGolem;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class AnimatedStoneGolem extends BaseAbility implements IImpact {

    public AnimatedStoneGolem() {
        super(LibNames.ANIMATED_STONE_GOLEM, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (hasNoCooldown(skillInfo) && canActivate(owner)) {
                RayTraceHelper.getFloorLookedAt(owner, 5, 5).map(BlockPos::up).ifPresent(pos -> {
                    InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                    InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                    int level = infoUpgradeable.getLevel();
                    if (infoCooldown.canSetCooldown(owner)) {
                        infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                    }

                    //
                    int stunTime = DSLDefaults.getStun(this, level);
                    SkillData status = SkillData.of(ModEffects.STUNNED)
                            .by(owner)
                            .with(stunTime)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();

                    float health = DSLDefaults.getHealth(this, level);
                    float damageMirror = DSLDefaults.getDamageMimicry(this, level);
                    float damage = DSLDefaults.getDamage(this, level);
                    EntityStoneGolem golem = new EntityStoneGolem(owner.world);
                    golem.setData(status);
                    golem.setPosition(pos.getX() + 0.5D, pos.getY() + 0.01D, pos.getZ() + 0.5D);
                    golem.setOwnerId(owner.getUniqueID());
                    golem.setMaxHealth(health);
                    golem.setHealth(health);
                    golem.setMirrorDamage(damageMirror);
                    golem.setDamage(damage);
                    golem.spawnEntity();

                    int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
                    NBTTagCompound compound = new NBTTagCompound();
                    NBTHelper.setEntity(compound, owner, "owner");
                    NBTHelper.setEntity(compound, golem, "golem");
                    SkillData data = SkillData.of(this)
                            .by(owner)
                            .with(time)
                            .put(compound)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();
                    super.apply(owner, data);
                    super.sync(owner, data);

                    SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.ANIMATED_STONE);
                    super.sync(owner);
                });
            }
        } else {
            SkillHelper.getActiveEntityFrom(owner, this, EntityStoneGolem.class, "golem").ifPresent(e -> {
                e.teleportTo(owner);
                e.setRevengeTarget(null);
                e.setAttackTarget(null);
                owner.setLastAttackedEntity(null);
            });
        }
    }

    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        //Do nothing
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (NBTHelper.getEntity(EntityStoneGolem.class, data.nbt, "golem") == null) {
           super.unapply(owner, data);
            super.async(owner, data);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.EARTH_DEFENSE_CONFIG + LibNames.ANIMATED_STONE_GOLEM;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
