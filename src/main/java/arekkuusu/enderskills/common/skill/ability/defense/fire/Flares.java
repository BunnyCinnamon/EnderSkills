package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.Capabilities;
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
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.sounds.FlaresSound;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class Flares extends BaseAbility implements IImpact {

    public Flares() {
        super(LibNames.FLARES, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (hasNoCooldown(skillInfo) && canActivate(owner)) {
                InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                int level = infoUpgradeable.getLevel();
                if (infoCooldown.canSetCooldown(owner)) {
                    infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                }

                //
                double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
                double damage = DSLDefaults.getDamage(this, level);
                int interval = DSLDefaults.triggerIntervalDuration(owner, this, level).getAmount();
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setDouble(compound, "range", range);
                NBTHelper.setDouble(compound, "damage", damage);
                NBTHelper.setInteger(compound, "interval", interval);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                super.unapply(owner, data);
                super.async(owner, data);
            });
        }
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new FlaresSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (tick % 20 == 0 && (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode)) {
            Capabilities.endurance(owner).ifPresent(capability -> {
                int level = Capabilities.get(owner).flatMap(a -> a.getOwned(this)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                int drain = ModAttributes.ENDURANCE.getEnduranceDrain(this, level);
                if (capability.getEndurance() - drain >= 0) {
                    capability.setEndurance(capability.getEndurance() - drain);
                    capability.setEnduranceDelay(30);
                    if (owner instanceof EntityPlayerMP) {
                        PacketHelper.sendEnduranceSync((EntityPlayerMP) owner);
                    }
                } else {
                    super.unapply(owner, data);
                    super.async(owner, data);
                }
            });
        }

        int interval = NBTHelper.getInteger(data.nbt, "interval");
        if (tick % interval == 0) {
            double distance = NBTHelper.getDouble(data.nbt, "range") * MathHelper.clamp(((double) tick / 10D), 0D, 1D);
            Vec3d pos = owner.getPositionVector();
            pos = new Vec3d(pos.x, pos.y + owner.height / 2, pos.z);
            Vec3d min = pos.subtract(0.5D, 0.5D, 0.5D);
            Vec3d max = pos.addVector(0.5D, 0.5D, 0.5D);
            AxisAlignedBB bb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
            List<EntityLivingBase> list = owner.world.getEntitiesWithinAABB(EntityLivingBase.class, bb.grow(distance), TeamHelper.SELECTOR_ENEMY.apply(owner));
            if (!list.isEmpty()) {
                EntityLivingBase target = list.get(0);
                for (EntityLivingBase entity : list) {
                    if (entity.getDistance(owner) < target.getDistance(owner)) {
                        target = entity;
                    }
                }
                SkillData dataCopy = SkillData.of(this).put(data.nbt).create();
                EntityThrowableData throwable = new EntityThrowableData(owner.world, owner, distance, dataCopy, false);
                throwable.setOwnerId(owner.getUniqueID());
                throwable.setFollowId(target.getUniqueID());
                Vec3d motion = Vector.Up.rotateRandom(owner.world.rand, 120).multiply(0.2 + owner.world.rand.nextDouble() * 0.1).toVec3d();
                throwable.motionX = motion.x;
                throwable.motionY = motion.y;
                throwable.motionZ = motion.z;
                throwable.posY += 0.5;
                owner.world.spawnEntity(throwable);

                SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.FLARES);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit instanceof EntityLivingBase && owner != null) {
            double damage = skillData.nbt.getDouble("damage");
            SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
            damageSource.setMagicDamage();
            SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
            MinecraftForge.EVENT_BUS.post(event);
            trace.entityHit.attackEntityFrom(event.getSource(), (float) event.getAmount());
        }
    }
    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.FLARES;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
