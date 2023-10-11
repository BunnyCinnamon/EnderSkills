package arekkuusu.enderskills.common.skill.ability.offence.blackflame;

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
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.effect.BlackFlame;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class BlackRagingFlameBall extends BaseAbility implements IImpact, IScanEntities, IExpand, IFindEntity {

    public BlackRagingFlameBall() {
        super(LibNames.BLACK_RAGING_FLAME_BALL, new Properties());
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
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        double true_damage = DSLDefaults.getTrueDamage(this, level);
        int dotDuration = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();
        double dot = DSLDefaults.getDamageOverTime(this, level);
        double true_dot = DSLDefaults.getTrueDamageOverTime(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "true_damage", true_damage);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setInteger(compound, "time", time);
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setDouble(compound, "true_dot", true_dot);
        NBTHelper.setInteger(compound, "dotDuration", dotDuration);

        SkillData data = SkillData.of(this)
                .with(dotDuration)
                .put(compound)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, 0.05F, true);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.FIREBALL);
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
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, skillData, time + 5);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        spawn.growTicks = 5;
        source.world.spawnEntity(spawn); //MANIFEST B L O O D!!

        spawn.world.newExplosion(spawn, spawn.posX, spawn.posY, spawn.posZ, (float) radius / 5F, false, CommonConfig.getSyncValues().skill.destroyBlocks);

        SoundHelper.playSound(source.world, new BlockPos(hitVector.x, hitVector.y, hitVector.z), ModSounds.FIREBALL_EXPLODE);
    }

    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount * 0.15, amount, amount * 0.15);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!target.world.isRemote) {
            ModEffects.BLACK_FLAME.set(target, skillData);
        }
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!target.world.isRemote && !SkillHelper.isActive(target, this)) {
            super.apply(target, skillData);

            SoundHelper.playSound(source.world, source.getPosition(), ModSounds.FIRE_HIT);
        }
    }

    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("damage");
        double true_damage = data.nbt.getDouble("true_damage");
        BlackFlame.dealDamage(this, entity, owner, damage);
        BlackFlame.dealTrueDamage(this, entity, owner, true_damage);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLACK_FLAME_OFFENCE_CONFIG + LibNames.BLACK_RAGING_FLAME_BALL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
