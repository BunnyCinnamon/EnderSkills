package arekkuusu.enderskills.common.skill.ability.offence.light;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.entity.EntitySolarLance;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

public class SolarLance extends BaseAbility {

    public SolarLance() {
        super(LibNames.SOLAR_LANCE, new Properties());
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
                double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
                double range = DSLDefaults.triggerSize(owner, this, level).getAmount();
                int piercing = SolarLance.getPiercing(level);
                double damage = DSLDefaults.getDamage(this, level);
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setDouble(compound, "damage", damage);
                NBTHelper.setDouble(compound, "distance", distance);
                NBTHelper.setDouble(compound, "range", range);
                NBTHelper.setDouble(compound, "piercing", piercing);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                EntitySolarLance spawn = new EntitySolarLance(owner.world, owner, data, (float) distance);

                Vector direction = new Vector(owner.getLookVec()).normalize();
                Vector perpendicular = direction.perpendicular().normalize();
                Quat quat = Quat.fromAxisAngleRad(direction, (float) Math.toRadians(360D * owner.world.rand.nextDouble()));
                Vector rotatedPerp = perpendicular.rotate(quat).normalize().multiply(0.25);

                MotionHelper.ayylmaoMotion(owner, spawn, distance, (int) (10D + (distance / 20D)));
                spawn.setPosition(owner.posX + rotatedPerp.x, owner.posY + (owner.getEyeHeight() - 0.5) + rotatedPerp.y, owner.posZ + rotatedPerp.z);
                spawn.setRadius(range);
                spawn.penesMaximus = piercing;
                owner.world.spawnEntity(spawn);
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
    public void begin(EntityLivingBase target, SkillData data) {
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = NBTHelper.getDouble(data.nbt, "damage");
        SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        damageSource.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        target.attackEntityFrom(event.getSource(), event.toFloat());

        if (target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.OFFLIGHT_ONHIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_OFFENCE_CONFIG + LibNames.SOLAR_LANCE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }

    public static int getPiercing(int level) {
        return DSLEvaluator.evaluateInt(ModAbilities.SOLAR_LANCE, "PIERCING", level, 1D);
    }
    /*Config Section*/
}
