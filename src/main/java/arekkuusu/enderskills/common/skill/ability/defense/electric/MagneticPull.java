package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.client.sounds.MagneticPullSound;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.ILoopSound;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class MagneticPull extends BaseAbility implements IScanEntities, ILoopSound, IImpact, IExpand {

    public MagneticPull() {
        super(LibNames.MAGNETIC_PULL, new Properties());
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

        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double range = DSLDefaults.triggerSize(owner, this, level).getAmount();
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double stun = DSLDefaults.getStun(this, level);
        double slow = DSLDefaults.getSlow(this, level);
        double pull = DSLDefaults.getForce(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "time", time);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "distance", distance);
        NBTHelper.setDouble(compound, "stun", stun);
        NBTHelper.setDouble(compound, "slow", slow);
        NBTHelper.setDouble(compound, "pull", pull);
        SkillData data = SkillData.of(this)
                .put(compound)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, false);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.ELECTRIC_HIT);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        int time = skillData.nbt.getInteger("time");
        double radius = skillData.nbt.getDouble("range");
        Vec3d hitVector = trace.hitVec;
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
            hitVector = new Vec3d(hitVector.x, hitVector.y + trace.entityHit.getEyeHeight(), hitVector.z);
        }
        SkillData status = SkillData.of(this)
                .by(skillData.id + ":" + skillData.skill.getRegistryName())
                .with(1)
                .put(skillData.nbt.copy(), skillData.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, status, time + 5);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        spawn.setGrowTicks(5);
        spawn.spawnEntity(); //MANIFEST B L O O D!!

        if (owner != null) {
            SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.RADIANT_RAY_RELEASE);
        } else {
            SoundHelper.playSound(source.world, source.getPosition(), ModSounds.RADIANT_RAY_RELEASE);
        }
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        double slow = NBTHelper.getDouble(skillData.nbt, "slow");
        double pull = NBTHelper.getDouble(skillData.nbt, "pull");
        int stun = NBTHelper.getInteger(skillData.nbt, "stun");
        if (!isClientWorld(target)) {
            if (SkillHelper.isActive(target, ModEffects.ELECTRIFIED)) {
                ModEffects.ELECTRIFIED.propagate(target, skillData, stun);
            }
            if (target.isWet() && source.ticksExisted % 20 == 0) {
                target.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 2);
            }
            ModEffects.SLOWED.set(target, skillData, slow);
        }
        if (!isClientWorld(target) || target instanceof EntityPlayer) {
            MotionHelper.pull(source.getPositionVector(), target, pull);
            if (target.collidedHorizontally) {
                target.motionY = 0;
            }
        }
    }
    //* Entity *//

    @Override
    @SideOnly(Side.CLIENT)
    public void makeSound(Entity source) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MagneticPullSound(source));
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.MAGNETIC_PULL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
