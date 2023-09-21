package arekkuusu.enderskills.common.skill.ability.offence.light;

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
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityFinalFlash;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class FinalFlash extends BaseAbility implements IScanEntities, IExpand {

    public FinalFlash() {
        super(LibNames.FINAL_FLASH, new Properties());
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
        int duration = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double dot = DSLDefaults.getDamageOverTime(this, level);
        double delay = DSLDefaults.getDelay(this, level);
        double size = DSLDefaults.triggerSize(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setDouble(compound, "delay", delay);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "duration", duration);
        NBTHelper.setDouble(compound, "size", size);
        SkillData data = SkillData.of(this)
                .by(owner)
                .put(compound)
                .create();
        EntityFinalFlash spawn = new EntityFinalFlash(owner.world, owner, data, duration);

        Vector direction = new Vector(owner.getLookVec()).normalize();
        Vector perpendicular = direction.perpendicular().normalize();
        Quat quat = Quat.fromAxisAngleRad(direction, (float) Math.toRadians(360D * owner.world.rand.nextDouble()));
        Vector rotatedPerp = perpendicular.rotate(quat).normalize().multiply(0.45);

        spawn.setPosition(owner.posX + rotatedPerp.x, owner.posY + owner.getEyeHeight() + rotatedPerp.y, owner.posZ + rotatedPerp.z);
        spawn.setRange(range);
        spawn.setRadius(size);
        owner.world.spawnEntity(spawn);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase target, SkillData data) {
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = NBTHelper.getDouble(data.nbt, "dot") / NBTHelper.getDouble(data.nbt, "duration");
        SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        damageSource.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        target.attackEntityFrom(event.getSource(), event.toFloat());

        if (target.ticksExisted % 10 == 0 && target.world instanceof WorldServer) {
            SoundHelper.playSound(target.world, target.getPosition(), ModSounds.OFFLIGHT_ONHIT);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_OFFENCE_CONFIG + LibNames.FINAL_FLASH;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
