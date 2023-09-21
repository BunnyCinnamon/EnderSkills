package arekkuusu.enderskills.common.skill.ability.offence.wind;

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
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableUpdraft;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class Updraft extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public Updraft() {
        super(LibNames.UPDRAFT, new Properties());
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
        double force = DSLDefaults.getForce(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "force", force);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "distance", distance);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(10)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityPlaceableUpdraft spawn = new EntityPlaceableUpdraft(owner.world, owner, data, (int) (2 * distance));
        MotionHelper.forwardMotion(owner, spawn, distance, (int) (2 * distance));
        spawn.setPosition(owner.posX, owner.posY, owner.posZ);
        spawn.setRadius(range);
        spawn.spawnEntity();
        super.sync(owner);

        SoundHelper.playSound(spawn.world, spawn.getPosition(), ModSounds.UPDRAFT);
    }

    //* Entity *//
    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount, 0, amount).expand(0, amount, 0);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        super.apply(target, skillData);
        sync(target, skillData);

        SoundHelper.playSound(target.world, target.getPosition(), ModSounds.WIND_ON_HIT);
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        double force = NBTHelper.getDouble(data.nbt, "force") / 10D;
        Vec3d pos = target.getPositionVector();
        target.setPositionAndUpdate(pos.x, pos.y + force, pos.z);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_OFFENCE_CONFIG + LibNames.UPDRAFT;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
