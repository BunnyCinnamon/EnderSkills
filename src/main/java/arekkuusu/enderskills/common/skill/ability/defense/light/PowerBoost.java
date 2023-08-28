package arekkuusu.enderskills.common.skill.ability.defense.light;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.*;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.*;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PowerBoost extends BaseAbility implements IImpact {

    public PowerBoost() {
        super(LibNames.POWER_BOOST, new Properties());
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
        double power = DSLDefaults.getPower(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "power", power);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityThrowableData.throwForTarget(owner, range, data, false);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.POWER_BOOST);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ALLY.apply(owner))) {
            apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);

            SoundHelper.playSound(trace.entityHit.world, trace.entityHit.getPosition(), ModSounds.LIGHT_HIT);
        }
    }
    //* Entity *//

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || !SkillHelper.isSkillDamage(event.getSource())) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isActive(this)) {
                capability.getActives().stream().filter(h -> h.data.skill == this).forEach(h -> {
                    double power = NBTHelper.getDouble(h.data.nbt, "power");
                    event.setAmount(event.getAmount() + (event.getAmount() * power));
                });
            }
        });
    }

    public static final List<String> GROW_LIST = new ArrayList<>();

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity instanceof EntityPlayer) return;
        Capabilities.powerBoost(entity).ifPresent(c -> {
            String key = entityToString(entity);
            if (GROW_LIST.contains(key)) {
                if (SkillHelper.isActive(entity, this)) {
                    float size = 0;

                    SkilledEntityCapability capability = Capabilities.get(event.getEntity()).orElse(null);
                    for (SkillHolder active : Objects.requireNonNull(capability).getActives()) {
                        if (active.data.skill == ModAbilities.POWER_BOOST) {
                            double power = NBTHelper.getDouble(active.data.nbt, "power");
                            if (size == 0) size = (float) power;
                            else size *= (float) power;
                        }
                    }
                    float scale = 1F + size;

                    if (!MathUtil.fuzzyEqual(c.eyeNew, c.eyeOriginal * scale)) {
                        c.eyeNew = c.eyeOriginal * scale;
                    }
                    if (!MathUtil.fuzzyEqual(c.widthNew, c.widthOriginal * scale) || !MathUtil.fuzzyEqual(c.heightNew, c.heightOriginal * scale)) {
                        setSize(entity, c.widthOriginal * scale, c.heightOriginal * scale);
                        c.widthNew = c.widthOriginal * scale;
                        c.heightNew = c.heightOriginal * scale;
                    }
                } else {
                    setSize(entity, c.widthOriginal, c.heightOriginal);
                    c.widthNew = 0;
                    c.heightNew = 0;
                    GROW_LIST.remove(key);
                }
            } else if (SkillHelper.isActive(entity, this)) {
                GROW_LIST.add(key);
                c.widthOriginal = entity.width;
                c.heightOriginal = entity.height;
            }
        });
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayer entity = event.player;
            Capabilities.powerBoost(entity).ifPresent(c -> {
                String key = entityToString(entity);
                if (GROW_LIST.contains(key)) {
                    if (SkillHelper.isActive(entity, this)) {
                        float size = 0;

                        SkilledEntityCapability capability = Capabilities.get(event.player).orElse(null);
                        for (SkillHolder active : Objects.requireNonNull(capability).getActives()) {
                            if (active.data.skill == ModAbilities.POWER_BOOST) {
                                double power = NBTHelper.getDouble(active.data.nbt, "power");
                                if (size == 0) size = (float) power;
                                else size *= (float) power;
                            }
                        }
                        float scale = 1F + size;

                        if (!MathUtil.fuzzyEqual(entity.eyeHeight, c.eyeOriginal * scale)) {
                            entity.eyeHeight = c.eyeOriginal * scale;
                            c.eyeNew = c.eyeOriginal * scale;
                        }
                        if (!MathUtil.fuzzyEqual(entity.width, c.widthOriginal * scale) || !MathUtil.fuzzyEqual(entity.height, c.heightOriginal * scale)) {
                            setSize(entity, c.widthOriginal * scale, c.heightOriginal * scale);
                            c.widthNew = c.widthOriginal * scale;
                            c.heightNew = c.heightOriginal * scale;
                        }
                    } else {
                        setSize(entity, c.widthOriginal, c.heightOriginal);
                        entity.eyeHeight = entity.getDefaultEyeHeight();
                        c.eyeNew = 0;
                        c.widthNew = 0;
                        c.heightNew = 0;
                        GROW_LIST.remove(key);
                    }
                } else if (SkillHelper.isActive(entity, this)) {
                    GROW_LIST.add(key);
                    c.widthOriginal = entity.width;
                    c.heightOriginal = entity.height;
                    c.eyeOriginal = entity.eyeHeight;
                }
            });
        }
    }

    public String entityToString(Entity entity) {
        return entity.getUniqueID() + ":" + entity.world.isRemote;
    }

    public void setSize(EntityLivingBase entity, float width, float height) {
        entity.width = width;
        entity.height = height;
        Vec3d pos = entity.getPositionVector();
        entity.setEntityBoundingBox(new AxisAlignedBB(pos.x - width / 2, pos.y, pos.z - width / 2, pos.x + width / 2, pos.y + entity.height, pos.z + width / 2));
        entity.resetPositionToBB();
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_DEFENSE_CONFIG + LibNames.POWER_BOOST;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
