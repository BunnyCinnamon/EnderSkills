package arekkuusu.enderskills.common.skill.ability.mobility.wind;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Fog extends BaseAbility {

    public Fog() {
        super(LibNames.FOG, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.FOG);
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        for (Entity entity : owner.world.loadedEntityList) {
            if (entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() == owner) {
                ((EntityLiving) entity).setAttackTarget(null);
            }
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.FOG);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.getSource().getDamageType().equals("ability")) return;
        DamageSource source = event.getSource();
        if (!source.getDamageType().matches("player|mob")) return;
        if (source.getTrueSource() == null || source instanceof SkillDamageSource || source.getImmediateSource() != source.getTrueSource())
            return;
        SkillHelper.getActiveFrom(event.getEntityLiving(), ModAbilities.FOG).ifPresent(data -> {
            super.unapply(event.getEntityLiving(), data);
            async(event.getEntityLiving(), data);
        });
    }

    @SubscribeEvent
    public void onSkillUse(SkillActivateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        SkillHelper.getActiveFrom(event.getEntityLiving(), ModAbilities.FOG).ifPresent(data -> {
            super.unapply(event.getEntityLiving(), data);
            async(event.getEntityLiving(), data);
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.FOG;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
