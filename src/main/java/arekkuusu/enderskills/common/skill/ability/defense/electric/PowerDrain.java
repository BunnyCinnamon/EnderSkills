package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.Capabilities;
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
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class PowerDrain extends BaseAbility implements IFindEntity, IExpand {

    public PowerDrain() {
        super(LibNames.POWER_DRAIN, new Properties());
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
        double power = DSLDefaults.getPower(this, level);
        double stun = DSLDefaults.getStun(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "stun", stun);
        NBTHelper.setDouble(compound, "power", power);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(INSTANT)
                .put(compound)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(owner.world, owner, data, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(owner.posX, owner.posY + owner.height / 2, owner.posZ);
        spawn.setRadius(range);
        spawn.spawnEntity();
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.POWER_DRAIN);
    }

    //* Entity *//
    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        return source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        super.apply(target, skillData);
        super.sync(target, skillData);
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!target.world.isRemote) {
            if (SkillHelper.isActive(target, ModEffects.ELECTRIFIED)) {
                int stun = NBTHelper.getInteger(skillData.nbt, "stun");
                ModEffects.ELECTRIFIED.propagate(target, skillData, stun);
            }
            if (source.isWet() && source.ticksExisted % 20 == 0) {
                source.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 2);
            }
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        EntityLivingBase owner = SkillHelper.getOwner(data);
        if (owner != null) {
            Capabilities.endurance(entity).ifPresent(capability -> {
                double power = NBTHelper.getDouble(data.nbt, "power");
                double a[] = {5 * 20};
                Capabilities.get(entity).flatMap(aaa -> aaa.getOwned(ModAttributes.ENDURANCE)).ifPresent(iii -> a[0] = DSLDefaults.getRegen(ModAttributes.ENDURANCE, ((AttributeInfo) iii).getLevel()));
                double drain = power - capability.drain(power, a[0]);
                if (drain > 0) {
                    if (!isClientWorld(entity)) {
                        if (entity instanceof EntityPlayerMP) {
                            PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                        }
                        EnderSkills.getProxy().addToQueue(() -> ModEffects.OVERCHARGE.set(owner, drain));
                        {
                            EntityLivingBase from = entity;
                            EntityLivingBase to = owner;
                            Vector posFrom = new Vector(from.getPositionVector()).addVector(from.world.rand.nextDouble() * 0.05D, from.height / 2D + from.world.rand.nextDouble() * 0.05D, from.world.rand.nextDouble() * 0.05D);
                            Vector posTo = new Vector(to.getPositionVector()).addVector(to.world.rand.nextDouble() * 0.05D, to.height / 2D + to.world.rand.nextDouble() * 0.05D, to.world.rand.nextDouble() * 0.05D);
                            EnderSkills.getProxy().spawnLightning(to.world, posFrom, posTo, 4, 0.6F, 5, 0xF4F389, false);

                            SoundHelper.playSound(to.world, new BlockPos(posTo.x, posTo.y, posTo.z), ModSounds.SPARK);
                        }
                    }
                }
            });
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.POWER_DRAIN;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
