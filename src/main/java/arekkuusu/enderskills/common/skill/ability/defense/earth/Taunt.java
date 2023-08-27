package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class Taunt extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public static final UUID TAUNT_UUID = UUID.fromString("c0fef459-78da-47df-8c6c-62c95c2f5609");

    public Taunt() {
        super(LibNames.TAUNT, new Properties());
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
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        SkillData data = SkillData.of(this)
                .by(TAUNT_UUID)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.ID)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(owner.world, owner, data, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(owner.posX, owner.posY + owner.height / 2, owner.posZ);
        spawn.setRadius(range);
        spawn.spawnEntity();
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.TAUNT);
    }

    //* Entity *//
    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        super.apply(target, skillData);
        super.sync(target, skillData);
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        if (isStunnedByAbility(target)) return;
        Optional.ofNullable(SkillHelper.getOwner(data)).ifPresent(owner -> {
            if (target instanceof EntityLiving) {
                ((EntityLiving) target).getNavigator().clearPath();
                if (((EntityLiving) target).getAttackTarget() != owner)
                    ((EntityLiving) target).setAttackTarget(owner);
            }
            if (target.collidedHorizontally) {
                if (target.onGround) {
                    target.motionY = 0.4F;
                }
            }
            target.motionX += (Math.signum(owner.posX - target.posX) * 0.5D - target.motionX) * 0.100000000372529;
            target.motionZ += (Math.signum(owner.posZ - target.posZ) * 0.5D - target.motionZ) * 0.100000000372529;
            if (target.isRiding()) {
                target.dismountRidingEntity();
            }
        });
    }

    public boolean isStunnedByAbility(EntityLivingBase entity) {
        return Capabilities.get(entity).map(c -> c.isActive(ModEffects.STUNNED)).orElse(false);
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillActionableEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void inputListener(InputUpdateEvent event) {
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.getMovementInput().forwardKeyDown = false;
            event.getMovementInput().rightKeyDown = false;
            event.getMovementInput().backKeyDown = false;
            event.getMovementInput().leftKeyDown = false;
            event.getMovementInput().sneak = false;
            event.getMovementInput().jump = false;
            event.getMovementInput().moveForward = 0;
            event.getMovementInput().moveStrafe = 0;
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.EARTH_DEFENSE_CONFIG + LibNames.TAUNT;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
