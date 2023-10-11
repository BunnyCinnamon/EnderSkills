package arekkuusu.enderskills.common.skill.ability.offence.wind;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.entity.EntityCrush;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class Crush extends BaseAbility implements IImpact {

    public Crush() {
        super(LibNames.CRUSH, new Properties());
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
        double size = DSLDefaults.triggerSize(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "size", size);
        NBTHelper.setDouble(compound, "damage", damage);
        SkillData data = SkillData.of(this)
                .put(compound)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, 3F, false);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.CRUSH);
    }

    //* Entity *//
    @Override
    @SuppressWarnings("ConstantConditions")
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        EntityCrush crush = new EntityCrush(source.world);
        crush.setPosition(trace.hitVec.x, trace.hitVec.y, trace.hitVec.z);
        crush.setData(skillData);
        crush.setYawFrom(owner);
        crush.setSize((float) NBTHelper.getDouble(skillData.nbt, "size"));
        crush.setDamage((float) NBTHelper.getDouble(skillData.nbt, "damage"));
        source.world.spawnEntity(crush); //MANIFEST C R U S H!!

        SoundHelper.playSound(source.world, source.getPosition(), ModSounds.WIND_ON_HIT);
    }
    //* Entity *//

    @Override
    public void apply(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        double damage = NBTHelper.getDouble(data.nbt, "damage");
        EntityLivingBase owner = SkillHelper.getOwner(data);
        SkillDamageSource damageSource = new SkillDamageSource(DAMAGE_HIT_TYPE, owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, ModAbilities.CRUSH, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getAmount() > 0) {
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_OFFENCE_CONFIG + LibNames.CRUSH;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
