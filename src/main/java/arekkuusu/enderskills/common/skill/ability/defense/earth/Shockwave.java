package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableShockwave;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.List;

public class Shockwave extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public Shockwave() {
        super(LibNames.SHOCKWAVE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasNoCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        if (owner.onGround) {
            InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
            InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
            int level = infoUpgradeable.getLevel();
            if (infoCooldown.canSetCooldown(owner)) {
                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
            }

            //
            int time = DSLDefaults.getStun(this, level);
            int range = DSLDefaults.triggerRange(owner, this, level).toInteger();
            int rangeExtra = DSLDefaults.triggerRangeExtension(owner, this, level).toInteger();
            double push = DSLDefaults.getForce(this, level);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setInteger(compound, "time", time);
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setDouble(compound, "range_extra", rangeExtra);
            NBTHelper.setDouble(compound, "push", push);
            NBTHelper.setVector(compound, "pusherVector", owner.getPositionVector());
            SkillData data = SkillData.of(this)
                    .with(10)
                    .put(compound)
                    .create();
            EntityPlaceableShockwave spawn = new EntityPlaceableShockwave(owner.world, owner, data, Math.max((int) (range + rangeExtra) * 5, EntityPlaceableData.MIN_TIME));
            spawn.setPosition(owner.posX, owner.posY, owner.posZ);
            spawn.setRadius(range + rangeExtra);
            spawn.spreadOnTerrain();
            spawn.spawnEntity();
            super.sync(owner);

            SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.SHOCKWAVE);
        }
    }

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        Vec3d pusherVector = NBTHelper.getVector(data.nbt, "pusherVector");
        double push = NBTHelper.getDouble(data.nbt, "push");
        MotionHelper.push(pusherVector, target, push);
        if (target.collidedHorizontally) {
            target.motionY = 0;
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EnderSkills.getProxy().addToQueue(() -> ModEffects.STUNNED.set(entity, data, data.nbt.getInteger("time")));
    }

    //* Entity *//
    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount, amount, amount);
    }

    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        List<Entity> entities = source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
        entities.removeIf(entity -> {
            boolean withinHeight = false;
            BlockPos entityPos = entity.getPosition();
            BlockPos pos = entityPos.down();
            IBlockState state = source.world.getBlockState(pos);
            if (state.getCollisionBoundingBox(source.world, pos) != Block.NULL_AABB) {
                withinHeight = pos.getY() - entity.posY <= 1;
            }
            return !withinHeight;
        });
        return entities;
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        double distance = source.getDistanceSq(target);
        double applyDistance = skillData.nbt.getDouble("range");
        double diminishingDistance = skillData.nbt.getDouble("range_extra");
        if (distance > applyDistance) {
            SkillData skillDataFinal = skillData.copy();
            skillDataFinal.time = Math.max((int) (skillDataFinal.time * (diminishingDistance / (diminishingDistance - (distance - applyDistance)))), 1);
            skillData = skillDataFinal;
        }
        super.apply(target, skillData);
        super.sync(target, skillData);

        SoundHelper.playSound(target.world, target.getPosition(), ModSounds.EARTH_HIT);
    }
    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.EARTH_DEFENSE_CONFIG + LibNames.SHOCKWAVE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
