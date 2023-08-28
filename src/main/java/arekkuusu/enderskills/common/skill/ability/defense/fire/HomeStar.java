package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDurationEvent;
import arekkuusu.enderskills.api.event.SkillRangeEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.client.sounds.HomeStarSound;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HomeStar extends BaseAbility {

    public HomeStar() {
        super(LibNames.HOME_STAR, new Properties());
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
        double pulseRange = HomeStar.getPulseRange(owner, level).getAmount();
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        int pulseTime = HomeStar.getPulseInterval(level);
        double dot = HomeStar.getPulseDamageOverTime(level);
        int dotDuration = HomeStar.getPulseDamageOverTimeDuration(owner, level).getAmount();
        double damage = HomeStar.getPulseDamage(level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setInteger(compound, "time", time);
        NBTHelper.setInteger(compound, "pulseTime", pulseTime);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "pulseRange", pulseRange);
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setInteger(compound, "dotDuration", dotDuration);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new HomeStarSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        data.nbt.setInteger("tick", tick);
        double progress = MathHelper.clamp((double) tick / (double) Math.min(data.time, EntityPlaceableData.MIN_TIME), 0D, 1D);
        double distance = NBTHelper.getDouble(data.nbt, "range") * progress;
        Vec3d pos = owner.getPositionVector();
        pos = new Vec3d(pos.x, pos.y + owner.height / 2, pos.z);
        Vec3d min = pos.subtract(0.5D, 0.5D, 0.5D);
        Vec3d max = pos.addVector(0.5D, 0.5D, 0.5D);
        AxisAlignedBB bb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
        owner.world.getEntitiesWithinAABB(EntityLivingBase.class, bb.grow(distance), TeamHelper.SELECTOR_ALLY.apply(owner)).forEach(target -> {
            if (!SkillHelper.isActive(target, ModEffects.PULSAR)) {
                EnderSkills.getProxy().addToQueue(() -> {
                    ModEffects.PULSAR.set(target, data);
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.HOME_STAR;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }

    public static double getPulseDamageOverTime(int level) {
        return DSLEvaluator.evaluateDouble(ModAbilities.HOME_STAR, "PULSE_DOT", level, CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public static int getPulseInterval(int level) {
        return DSLEvaluator.evaluateInt(ModAbilities.HOME_STAR, "PULSE_INTERVAL", level, 1D);
    }

    public static SkillRangeEvent getPulseRange(EntityLivingBase entityLivingBase, int level) {
        double original = DSLEvaluator.evaluateDouble(ModAbilities.HOME_STAR, "PULSE_RANGE", level, 1D);
        return SkillRangeEvent.trigger(entityLivingBase, ModAbilities.HOME_STAR, original);
    }

    public static SkillDurationEvent getPulseDamageOverTimeDuration(EntityLivingBase entityLivingBase, int level) {
        int original = DSLEvaluator.evaluateInt(ModAbilities.HOME_STAR, "PULSE_DOT_DURATION", level, 1D);
        return SkillDurationEvent.trigger(entityLivingBase, ModAbilities.HOME_STAR, original);
    }

    public static double getPulseDamage(int level) {
        return DSLEvaluator.evaluateDouble(ModAbilities.HOME_STAR, "PULSE_DAMAGE", level, 1D);
    }
    /*Config Section*/
}
