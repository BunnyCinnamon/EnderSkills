package arekkuusu.enderskills.common.skill.ability.mobility.wind;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SpeedBoost extends BaseAbility {

    public static final DynamicModifier SPEED_ATTRIBUTE = new DynamicModifier(
            "c6668d81-8274-418e-b598-37b2cabd3813",
            LibMod.MOD_ID + ":" + LibNames.SPEED_BOOST,
            SharedMonsterAttributes.MOVEMENT_SPEED,
            Constants.AttributeModifierOperation.ADD);

    public SpeedBoost() {
        super(LibNames.SPEED_BOOST, new Properties());
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
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double speed = DSLDefaults.getSpeed(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "speed", speed);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.SPEED);
    }

    public static final String NBT_STEP = LibMod.MOD_ID + ":stepHeight";
    public static final float STEP_HEIGHT = 0.60001F;
    public static final float STEP_HEIGHT_SNEAK = 1.25F;
    public static final float STEP_HEIGHT_DEFAULT = 0.6F;

    public static final List<String> STEP_LIST = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        String key = entityToString(entity);
        if (STEP_LIST.contains(key)) {
            if (SkillHelper.isActiveFrom(entity, ModAbilities.SPEED_BOOST)) {
                SkillHelper.getActiveFrom(entity, ModAbilities.SPEED_BOOST).ifPresent(data -> {
                    if (!isClientWorld(event.getEntityLiving())) {
                        double speed = NBTHelper.getDouble(data.nbt, "speed");
                        SPEED_ATTRIBUTE.apply(entity, speed);
                    }

                    if (entity.getEntityData().getFloat(NBT_STEP) == STEP_HEIGHT_DEFAULT) {
                        if (entity.isSneaking()) {
                            entity.stepHeight = STEP_HEIGHT;
                        } else {
                            entity.stepHeight = STEP_HEIGHT_SNEAK;
                        }
                    } else if (entity.stepHeight == STEP_HEIGHT_DEFAULT) {
                        entity.getEntityData().setFloat(NBT_STEP, entity.stepHeight);
                    }
                });
            } else {
                if (!isClientWorld(event.getEntityLiving())) {
                    SPEED_ATTRIBUTE.remove(entity);
                }

                if (entity.stepHeight == STEP_HEIGHT || entity.stepHeight == STEP_HEIGHT_SNEAK) {
                    entity.stepHeight = STEP_HEIGHT_DEFAULT;
                }
                STEP_LIST.remove(key);
            }
        } else if (SkillHelper.isActiveFrom(entity, ModAbilities.SPEED_BOOST)) {
            STEP_LIST.add(key);
            entity.getEntityData().setFloat(NBT_STEP, entity.stepHeight);
        }
    }

    public String entityToString(Entity entity) {
        return entity.getUniqueID().toString() + ":" + entity.world.isRemote;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.SPEED_BOOST;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
