package arekkuusu.enderskills.common.skill.ability.offence.wind;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.*;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class Pull extends BaseAbility implements IImpact {

    public Pull() {
        super(LibNames.PULL, new Properties());
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
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        int time = DSLDefaults.getStun(this, level);
        Vec3d lookVector = owner.getLook(1F);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setVector(compound, "origin", owner.getPositionVector());
        NBTHelper.setVector(compound, "vector", lookVector);
        NBTHelper.setInteger(compound, "time", time);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(10)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityThrowableData.throwForTarget(owner, distance, data, false);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.PULL);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit instanceof EntityLivingBase && owner != null) {
            Vec3d positionVector = NBTHelper.getVector(skillData.nbt, "origin");
            Vec3d lookVector = NBTHelper.getVector(skillData.nbt, "vector");
            Vec3d targetVector = positionVector.addVector(
                    lookVector.x * 2,
                    lookVector.y * 2,
                    lookVector.z * 2
            );
            NBTHelper.setDouble(skillData.nbt, "force", trace.entityHit.getDistance(targetVector.x, targetVector.y, targetVector.z));
            super.apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);

            SoundHelper.playSound(source.world, source.getPosition(), ModSounds.WIND_ON_HIT);
        }
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        Vec3d vector = NBTHelper.getVector(data.nbt, "vector").scale(-1);
        double distance = NBTHelper.getDouble(data.nbt, "force");
        Vec3d from = target.getPositionVector();
        Vec3d to = from.addVector(
                vector.x * distance,
                vector.y * distance,
                vector.z * distance
        );
        MotionHelper.moveEntity(to, from, target);
        if (target.collidedHorizontally) {
            target.motionY = 0;
        }
        RayTraceResult trace = RayTraceHelper.forwardsRaycast(target, true, true, target);
        Entity owner = NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "owner");
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ENEMY.apply(owner))) {
            if (!SkillHelper.isActive(trace.entityHit, this, data.id)) {
                SkillHolder holder = new SkillHolder(data.copy());
                holder.tick = tick;
                Capabilities.get(trace.entityHit).ifPresent(skills -> skills.activate(holder)); //Add to entity Server Side
                PacketHelper.sendSkillHolderUseResponsePacket((EntityLivingBase) trace.entityHit, holder);
            }
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EnderSkills.getProxy().addToQueue(() -> ModEffects.STUNNED.set(entity, data, data.nbt.getInteger("time")));
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_OFFENCE_CONFIG + LibNames.PULL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
