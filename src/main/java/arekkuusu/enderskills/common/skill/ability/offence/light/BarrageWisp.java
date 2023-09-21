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
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.entity.EntityWisp;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.UUID;

public class BarrageWisp extends BaseAbility implements IImpact {

    public BarrageWisp() {
        super(LibNames.BARRAGE_WISPS, new Properties());
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
        double damage = DSLDefaults.getDamage(this, level);
        double amount = BarrageWisp.getAmount(level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "range", distance);
        NBTHelper.setDouble(compound, "amount", amount);
        NBTHelper.setDouble(compound, "delay", 10);
        SkillData data = SkillData.of(this)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        float a = 360 / (float) amount;
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < amount; i++) {
            EntityWisp throwable = new EntityWisp(owner.world, owner, distance, data, false, uuid);
            throwable.setOwnerId(owner.getUniqueID());

            Vector direction = new Vector(owner.getLookVec()).normalize();
            Vector perpendicular = direction.perpendicular().normalize();
            Quat quat = Quat.fromAxisAngleRad(direction, (float) Math.toRadians(a * (float) i));
            Vector rotatedPerp = perpendicular.rotate(quat).normalize().multiply(0.25);

            throwable.motionX = rotatedPerp.x;
            throwable.motionY = rotatedPerp.y;
            throwable.motionZ = rotatedPerp.z;
            throwable.posY += 0.5;
            owner.world.spawnEntity(throwable);
        }
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.BARRAGE_WHISPS_CAST);
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
            if (SkillHelper.isActive(trace.entityHit, ModEffects.GLOWING)) {
                ModEffects.GLOWING.activate((EntityLivingBase) trace.entityHit, skillData);
            }

            SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.OFFLIGHT_ONHIT);
        }
    }
    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_OFFENCE_CONFIG + LibNames.BARRAGE_WISPS;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }

    public static double getAmount(int level) {
        return DSLEvaluator.evaluateDouble(ModAbilities.BARRAGE_WISPS, "AMOUNT", level, 1D);
    }
    /*Config Section*/
}
