package arekkuusu.enderskills.common.skill.ability.defense.light;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PowerBoost extends BaseAbility implements IImpact {

    public PowerBoost() {
        super(LibNames.POWER_BOOST, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        double distance = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getRange(abilityInfo));;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            int time = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));;
            double power = getPower(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "power", power);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(time)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            EntityThrowableData.throwForTarget(owner, distance, data, false);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.POWER_BOOST, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ALLY.apply(owner))) {
            apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);

            if (trace.entityHit.world instanceof WorldServer) {
                ((WorldServer) trace.entityHit.world).playSound(null, trace.entityHit.posX, trace.entityHit.posY, trace.entityHit.posZ, ModSounds.LIGHT_HIT, SoundCategory.PLAYERS, 5.0F, (1.0F + (trace.entityHit.world.rand.nextFloat() - trace.entityHit.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
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
        return entity.getUniqueID().toString() + ":" + entity.world.isRemote;
    }

    public void setSize(EntityLivingBase entity, float width, float height) {
        entity.width = width;
        entity.height = height;
        Vec3d pos = entity.getPositionVector();
        entity.setEntityBoundingBox(new AxisAlignedBB(pos.x - width / 2, pos.y, pos.z - width / 2, pos.x + width / 2, pos.y + entity.height, pos.z + width / 2));
        entity.resetPositionToBB();
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public float getPower(AbilityInfo info) {
        return (float) this.config.get(this, "BOOST", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "DURATION", info.getLevel());
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add(TextHelper.translate("desc.stats.shift"));
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max", getMaxLevel()));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getPower(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        if (abilityInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                                AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                                infoNew.setLevel(infoNew.getLevel() + 1);
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                                description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getPower(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.CHARM;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_DEFENSE_CONFIG + LibNames.POWER_BOOST;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static final Configuration.Values CONFIG = new Configuration.Values();

        public static class Values {

            public String[] dsl = {
                    "",
                    "┌ v1.0",
                    "│ ",
                    "├ min_level: 0",
                    "├ max_level: 50",
                    "└ ",
                    "",
                    "┌ COOLDOWN (",
                    "│     shape: flat",
                    "│     min: 35s",
                    "│     max: 16s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   23s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   18s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ RANGE (",
                    "│     shape: flat",
                    "│     min: 8b",
                    "│     max: 24b",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   18b",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   20b",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DURATION (",
                    "│     shape: flat",
                    "│     min: 6s",
                    "│     max: 14s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   10s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   12s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ BOOST (",
                    "│     shape: flat",
                    "│     min: 10%",
                    "│     max: 100%",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   25%",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   50%",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 0",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: 300",
                    "│     ]",
                    "│ ",
                    "│     {1 to 50} [",
                    "│         shape: multiply 6",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
