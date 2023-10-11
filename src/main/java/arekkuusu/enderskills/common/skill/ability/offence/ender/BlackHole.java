package arekkuusu.enderskills.common.skill.ability.offence.ender;

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
import arekkuusu.enderskills.common.entity.EntityBlackHole;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;

public class BlackHole extends BaseAbility implements IImpact {

    public BlackHole() {
        super(LibNames.BLACK_HOLE, new Properties());
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
        double dot = DSLDefaults.getDamageOverTime(this, level);
        int dotDuration = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setInteger(compound, "time", time);
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setInteger(compound, "dotDuration", dotDuration);

        SkillData data = SkillData.of(this)
                .put(compound)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, 3F, false);
        super.sync(owner);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        Vec3d hitVector = trace.hitVec;

        int time = skillData.nbt.getInteger("time");
        double radius = skillData.nbt.getDouble("range");
        EntityBlackHole spawn = new EntityBlackHole(source.world, owner, skillData, time);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        source.world.spawnEntity(spawn);

        if (CommonConfig.getSyncValues().skill.destroyBlocks)
            spawn.world.createExplosion(spawn, spawn.posX, spawn.posY, spawn.posZ, (float) radius, true);

        SoundHelper.playSound(spawn.world, spawn.getPosition(), ModSounds.BLACKHOLE);
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = NBTHelper.getDouble(data.nbt, "damage");
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        source.setExplosion();
        SkillDamageEvent event = new SkillDamageEvent(owner, ModAbilities.BLACK_HOLE, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.toFloat() > 0 && event.toFloat() < Double.MAX_VALUE) {
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_OFFENCE_CONFIG + LibNames.BLACK_HOLE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
