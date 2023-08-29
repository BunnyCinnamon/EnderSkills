package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.sounds.UnstablePortalSound;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.ILoopSound;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class UnstablePortal extends BaseAbility implements IImpact, IExpand, IScanEntities, ILoopSound {

    public UnstablePortal() {
        super(LibNames.UNSTABLE_PORTAL, new Properties());
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
        double teleport = DSLDefaults.getDisplacement(this, level);
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "teleport", teleport);
        NBTHelper.setInteger(compound, "time", time);

        SkillData data = SkillData.of(this)
                .by(owner)
                .with(INSTANT)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, false);
        super.sync(owner);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        Vec3d hitVector = trace.hitVec;

        int time = skillData.nbt.getInteger("time");
        double radius = skillData.nbt.getDouble("range");
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, skillData, time);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        source.world.spawnEntity(spawn); //MANIFEST!!
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeSound(Entity source) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new UnstablePortalSound((EntityPlaceableData) source));
    }

    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount);
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!target.world.isRemote) {
            super.apply(target, skillData);
            sync(target, skillData);
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        double distance = NBTHelper.getDouble(data.nbt, "teleport");
        double x = entity.posX + (entity.getRNG().nextDouble() - 0.5D) * distance;
        double y = entity.posY + (((entity.getRNG().nextDouble()) * distance) - (distance / 2));
        double z = entity.posZ + (entity.getRNG().nextDouble() - 0.5D) * distance;

        for (int i = 0; i < 16; ++i) {
            double d3 = x + (entity.getRNG().nextDouble() - 0.5D) * distance;
            double d4 = MathHelper.clamp(y + (((entity.getRNG().nextDouble()) * distance) - (distance / 2)), 0.0D, entity.world.getActualHeight() - 1);
            double d5 = z + (entity.getRNG().nextDouble() - 0.5D) * distance;
            if (entity.isRiding()) {
                entity.dismountRidingEntity();
            }

            if (entity.attemptTeleport(d3, d4, d5)) {
                entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.UNSTABLE_PORTAL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
