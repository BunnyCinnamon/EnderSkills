package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.EntityStoneGolem;
import arekkuusu.enderskills.common.entity.data.IImpact;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AnimatedStoneGolem extends BaseAbility implements IImpact {

    public AnimatedStoneGolem() {
        super(LibNames.ANIMATED_STONE_GOLEM, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            RayTraceHelper.getFloorLookedAt(owner, 5, 5).ifPresent(pos -> {
                pos = pos.up();
                if (!((IInfoCooldown) skillInfo).hasCooldown() && isActionable(owner) && canActivate(owner)) {
                    if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                        abilityInfo.setCooldown(getCooldown(abilityInfo));
                    }
                    EntityStoneGolem golem = new EntityStoneGolem(owner.world);
                    int stunTime = getStunTime(abilityInfo);
                    SkillData status = SkillData.of(ModEffects.STUNNED)
                            .by(owner)
                            .with(stunTime)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();
                    golem.setData(status);
                    golem.setPosition(pos.getX() + 0.5D, pos.getY() + 0.01D, pos.getZ() + 0.5D);
                    golem.setOwnerId(owner.getUniqueID());
                    golem.setMaxHealth(getHealth(abilityInfo));
                    golem.setHealth(getHealth(abilityInfo));
                    golem.setMirrorDamage(getMirror(abilityInfo));
                    golem.setDamage(getDamage(abilityInfo));
                    owner.world.spawnEntity(golem);

                    int time = getTime(abilityInfo);
                    NBTTagCompound compound = new NBTTagCompound();
                    NBTHelper.setEntity(compound, owner, "owner");
                    NBTHelper.setEntity(compound, golem, "golem");
                    SkillData data = SkillData.of(this)
                            .by(owner)
                            .with(time)
                            .put(compound)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();
                    EntityThrowableData.throwFor(owner, 5, SkillData.of(this).create(), false);
                    apply(owner, data);
                    sync(owner, data);
                    sync(owner);

                    if (owner.world instanceof WorldServer) {
                        ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.ANIMATED_STONE, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                }
            });
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityStoneGolem.class, data.nbt, "golem")).ifPresent(e -> {
                    e.teleportTo(owner);
                    e.setRevengeTarget(null);
                    e.setAttackTarget(null);
                    owner.setLastAttackedEntity(null);
                });
            });
        }
    }

    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        //Do nothing
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (NBTHelper.getEntity(EntityStoneGolem.class, data.nbt, "golem") == null) {
            unapply(owner, data);
            async(owner, data);
        }
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public float getDamage(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.damage, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalPositiveEffect);
        return (float) (result * getEffectiveness());
    }

    public float getMirror(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.mirror, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalPositiveEffect);
        return (float) (result * getEffectiveness());
    }

    public float getHealth(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.health, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalNegativeEffect);
        return (float) (result * getEffectiveness());
    }

    public int getStunTime(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.health, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalTime);
        return (int) (result * getEffectiveness());
    }

    public int getCooldown(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().cooldown, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalCooldown);
        return (int) (result * getEffectiveness());
    }

    public int getTime(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().time, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalTime);
        return (int) (result * getEffectiveness());
    }

    public double getEffectiveness() {
        return Configuration.getSyncValues().effectiveness * CommonConfig.getSyncValues().skill.globalEffectiveness;
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
                        description.add(TextHelper.translate("desc.stats.mirror_damage", TextHelper.format2FloatPoint(getMirror(abilityInfo) * 100D), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getStunTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        description.add(TextHelper.translate("desc.stats.damage", TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.mirror_damage", TextHelper.format2FloatPoint(getMirror(infoNew) * 100D), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getStunTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            description.add(TextHelper.translate("desc.stats.damage", TextHelper.format2FloatPoint(getDamage(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getCostIncrement(EntityLivingBase entity, int total) {
        Optional<AdvancementCapability> optional = Capabilities.advancement(entity);
        if (optional.isPresent()) {
            AdvancementCapability advancement = optional.get();
            List<Skill> skillUnlockOrder = Arrays.asList(advancement.skillUnlockOrder);
            int index = skillUnlockOrder.indexOf(ModAbilities.TAUNT);
            if (index == -1) {
                index = advancement.skillUnlockOrder.length;
            }
            return (int) (total * (1D + index * CommonConfig.getSyncValues().advancement.xp.costIncrement));
        }
        return total;
    }

    @Override
    public int getUpgradeCost(@Nullable AbilityInfo info) {
        int level = info != null ? getLevel(info) + 1 : 0;
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().advancement.upgrade, level, levelMax);
        return (int) (func * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }
    /*Advancement Section*/

    @Override
    public void initSyncConfig() {
        Configuration.getSyncValues().maxLevel = Configuration.getValues().maxLevel;
        Configuration.getSyncValues().cooldown = Configuration.getValues().cooldown;
        Configuration.getSyncValues().time = Configuration.getValues().time;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().extra.damage = Configuration.getValues().extra.damage;
        Configuration.getSyncValues().extra.mirror = Configuration.getValues().extra.mirror;
        Configuration.getSyncValues().extra.stunTime = Configuration.getValues().extra.stunTime;
        Configuration.getSyncValues().extra.health = Configuration.getValues().extra.health;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "extra.damage", Configuration.getValues().extra.damage);
        NBTHelper.setArray(compound, "extra.mirror", Configuration.getValues().extra.mirror);
        NBTHelper.setArray(compound, "extra.stunTime", Configuration.getValues().extra.stunTime);
        NBTHelper.setArray(compound, "extra.health", Configuration.getValues().extra.health);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.damage = NBTHelper.getArray(compound, "extra.damage");
        Configuration.getSyncValues().extra.mirror = NBTHelper.getArray(compound, "extra.mirror");
        Configuration.getSyncValues().extra.stunTime = NBTHelper.getArray(compound, "extra.stunTime");
        Configuration.getSyncValues().extra.health = NBTHelper.getArray(compound, "extra.health");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.ANIMATED_STONE_GOLEM)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.ANIMATED_STONE_GOLEM)
        public static Values CONFIG = new Values();

        public static Values getValues() {
            return CONFIG;
        }

        @Config.Ignore
        protected static Values CONFIG_SYNC = new Values();

        public static Values getSyncValues() {
            return CONFIG_SYNC;
        }

        public static class Values {
            @Config.Comment("Skill specific extra Configuration")
            public final Extra extra = new Extra();
            @Config.Comment("Skill specific Advancement Configuration")
            public final Advancement advancement = new Advancement();

            @Config.Comment("Max level obtainable")
            @Config.RangeInt(min = 0)
            public int maxLevel = 50;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){105 * 20 + 15 * 20 * (1 - ((1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))))}",
                    "(25+){100 * 20 + 5 * 20 * (1- (((e^(0.1 * ((x-24) / (y-24))) - 1)/((e^0.1) - 1))))}",
                    "(50){90 * 20}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){45 * 20 + 25 * 20 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                    "(25+){70 * 20 + 10 * 20 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}",
                    "(50){90 * 20}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Golem Damage Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] damage = {
                        "(0+){5 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (6.44 - 5)}",
                        "(25+){6.44 + ((e^(3.25 * ((x-24) / (y-24))) - 1)/((e^3.25) - 1)) * (22 - 6.44)}",
                        "(50){25}"
                };
                @Config.Comment("Golem Damage Multiplier Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] mirror = {
                        "(0+){0.75 + ((e^(2.1 * (x / y)) - 1)/((e^2.1) - 1)) * (1.5 - 0.75)}"
                };
                @Config.Comment("Golem Health Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] stunTime = {
                        "(0+){3 * 20}"
                };
                @Config.Comment("Golem Health Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] health = {
                        "(0+){150 + (100 * 0.1 * x)}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){900}",
                        "(1+){4 * x}",
                        "(50){4 * x + 4 * x * 0.1}"
                };
            }
        }
    }
}
