package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ElectricPulse extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public ElectricPulse() {
        super(LibNames.ELECTRIC_PULSE, new Properties());
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
        double stun = DSLDefaults.getStun(this, level);
        double slow = DSLDefaults.getSlow(this, level);
        double push = DSLDefaults.getForce(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "stun", stun);
        NBTHelper.setDouble(compound, "slow", slow);
        NBTHelper.setDouble(compound, "push", push);
        NBTHelper.setVector(compound, "pusherVector", owner.getPositionVector());
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(10)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(owner.world, owner, data, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(owner.posX, owner.posY + owner.height / 2, owner.posZ);
        spawn.setRadius(range);
        spawn.spawnEntity();
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.ELECTRIC_PULSE);
    }

    //* Entity *//
    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (SkillHelper.isActive(target, ModEffects.ELECTRIFIED)) {
            int stun = NBTHelper.getInteger(skillData.nbt, "stun");
            ModEffects.ELECTRIFIED.propagate(target, skillData, stun);
        } else {
            ModEffects.ELECTRIFIED.set(target, skillData);
        }
        if (target.isWet()) {
            target.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 2);
        }
        super.apply(target, skillData);
        super.sync(target, skillData);
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        EnderSkills.getProxy().addToQueue(() -> ModEffects.SLOWED.set(entity, data, data.nbt.getDouble("slowed")));
        if (isClientWorld(entity) && !(entity instanceof EntityPlayer)) return;
        Vec3d pusherVector = NBTHelper.getVector(data.nbt, "pusherVector");
        double push = NBTHelper.getDouble(data.nbt, "push");
        MotionHelper.push(pusherVector, entity, push);
        if (entity.collidedHorizontally) {
            entity.motionY = 0;
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.ELECTRIC_PULSE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
