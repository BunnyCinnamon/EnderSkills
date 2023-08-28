package arekkuusu.enderskills.common.skill.ability.offence.fire;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.client.gui.data.SkillAdvancement;
import arekkuusu.enderskills.client.sounds.FlamingRainSound;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.*;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class FocusFlame extends BaseAbility implements IImpact, ILoopSound, IScanEntities, IExpand, IFindEntity {

    public FocusFlame() {
        super(LibNames.FOCUS_FLAME, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((InfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        double distance = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getRange(abilityInfo));;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double range = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getFlameRange(abilityInfo));;
            int time = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getFlameDuration(abilityInfo));
            double damage = getDamage(abilityInfo);
            int dotDuration = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));
            double dot = getDoT(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "damage", damage);
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setInteger(compound, "time", time);
            NBTHelper.setDouble(compound, "dot", dot);
            NBTHelper.setInteger(compound, "dotDuration", dotDuration);
            SkillData data = SkillData.of(this)
                    .with(dotDuration)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            EntityThrowableData.throwFor(owner, distance, data, false);
            super.sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.FOCUS_FLAME, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit != RayTraceResult.Type.MISS) {
            Vec3d hitVector = trace.hitVec;

            int time = skillData.nbt.getInteger("time");
            double radius = skillData.nbt.getDouble("range");
            EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, skillData, time);
            spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
            spawn.setRadius(radius);
            source.world.spawnEntity(spawn);

            if (spawn.world instanceof WorldServer) {
                ((WorldServer) spawn.world).playSound(null, spawn.posX, spawn.posY, spawn.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (spawn.world.rand.nextFloat() - spawn.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeSound(Entity source) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new FlamingRainSound((EntityPlaceableData) source));
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if(!target.world.isRemote) {
            ModEffects.BURNING.set(target, skillData);
           super.apply(target, skillData);
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("damage");
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        source.setExplosion();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public int getTopLevel() {
        return this.config.limit_level;
    }

    public float getFlameRange(AbilityInfo info) {
        return (float) this.config.get(this, "SIZE", info.getLevel());
    }

    public int getFlameDuration(AbilityInfo info) {
        return (int) this.config.get(this, "DURATION", info.getLevel());
    }

        public double getDoT(AbilityInfo info) {
        return this.config.get(this, "DOT", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public double getDamage(AbilityInfo info) {
        return this.config.get(this, "DAMAGE", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "DOT_DURATION", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this, abilityInfo.getLevel()))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max", getMaxLevel()));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.explode_range", TextHelper.format2FloatPoint(getFlameRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.initial_dot", TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
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
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.explode_range", TextHelper.format2FloatPoint(getFlameRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.initial_dot", TextHelper.format2FloatPoint(getDamage(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                                description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.FIRE_SPIRIT;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }

    @Override
    public int getEndurance(int lvl) {
        return (int) this.config.get(this, "ENDURANCE", lvl);
    }

    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_OFFENCE_CONFIG + LibNames.FOCUS_FLAME;
    public DSLConfig config = new DSLConfig();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
        initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
        sigmaDic();
    }

    @Override
    public void sigmaDic() {
        this.config = DSLParser.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static Configuration.Values CONFIG = new Configuration.Values();

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
                    "│     min: 86s",
                    "│     max: 25s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   64s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   30s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ RANGE (",
                    "│     shape: none",
                    "│     value: 90b",
                    "└ )",
                    "",
                    "┌ SIZE (",
                    "│     shape: none",
                    "│     value: 2b",
                    "└ )",
                    "",
                    "┌ DURATION (",
                    "│     shape: none",
                    "│     value: 4s",
                    "└ )",
                    "",
                    "┌ DAMAGE (",
                    "│     shape: flat",
                    "│     min: 6h",
                    "│     max: 8h",
                    "│ ",
                    "│     {0 to 49} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   7h",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DOT (",
                    "│     shape: flat",
                    "│     min: 1h",
                    "│     max: 4h",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   2h",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   3h",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DOT_DURATION (",
                    "│     shape: none",
                    "│     value: 4s",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 0",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: 600",
                    "│     ]",
                    "│ ",
                    "│     {1 to 50} [",
                    "│         shape: multiply 8",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
