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
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableSlash;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class Slash extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public Slash() {
        super(LibNames.SLASH, new Properties());
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
        double range = DSLDefaults.triggerSize(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "distance", distance);
        SkillData data = SkillData.of(this)
                .by(owner)
                .put(compound)
                .create();
        EntityPlaceableSlash spawn = new EntityPlaceableSlash(owner.world, owner, data, (int) (distance));
        MotionHelper.forwardMotion(owner, spawn, distance, (int) (distance));
        spawn.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
        spawn.setGrowTicks(5);
        spawn.setRadius(range);
        owner.world.spawnEntity(spawn);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.SLASH);
    }

    //* Entity *//
    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount, amount / 3, amount).expand(0, 0, 0);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        double damage = NBTHelper.getDouble(skillData.nbt, "damage");
        SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        target.attackEntityFrom(event.getSource(), event.toFloat());

        SoundHelper.playSound(target.world, target.getPosition(), ModSounds.WIND_ON_HIT);
    }

    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_OFFENCE_CONFIG + LibNames.SLASH;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
