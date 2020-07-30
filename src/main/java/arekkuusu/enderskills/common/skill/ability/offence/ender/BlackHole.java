package arekkuusu.enderskills.common.skill.ability.offence.ender;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.IInfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.nbt.UUIDWatcher;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.EntityBlackHole;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlackHole extends BaseAbility implements IImpact, ISkillAdvancement {

    public BlackHole() {
        super(LibNames.BLACK_HOLE);
        setTexture(ResourceLibrary.BLACK_HOLE);
    }

    @Override
    public void use(EntityLivingBase user, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(user)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        double distance = getRange(abilityInfo);

        if (shouldUse(user) && canUse(user)) {
            if (!(user instanceof EntityPlayer) || !((EntityPlayer) user).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double range = getHoleRange(abilityInfo);
            int time = getHoleDuration(abilityInfo);
            double damage = getDamage(abilityInfo);
            double dot = getDoT(abilityInfo);
            int dotDuration = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setInteger(compound, "time", time);
            NBTHelper.setDouble(compound, "damage", damage);
            NBTHelper.setDouble(compound, "dot", dot);
            NBTHelper.setInteger(compound, "dotDuration", dotDuration);

            SkillData data = SkillData.of(this)
                    .with(dotDuration)
                    .put(compound, UUIDWatcher.INSTANCE)
                    .overrides(this)
                    .create();
            EntityThrowableData.throwFor(user, distance, data, false);
            sync(user);
        }
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

        if (spawn.world instanceof WorldServer) {
            ((WorldServer) spawn.world).playSound(null, spawn.posX, spawn.posY, spawn.posZ, ModSounds.BLACKHOLE, SoundCategory.PLAYERS, 1.0F, (1.0F + (spawn.world.rand.nextFloat() - spawn.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "user")).ifPresent(user -> {
            if (entity != user) {
                if (!isClientWorld(entity)) {
                    double damage = data.nbt.getDouble("dot");
                    double time = data.nbt.getInteger("dotDuration");
                    SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, user);
                    source.setMagicDamage();
                    SkillDamageEvent event = new SkillDamageEvent(user, this, source, damage);
                    MinecraftForge.EVENT_BUS.post(event);
                    entity.attackEntityFrom(event.getSource(), (float) (event.toFloat() / time));
                }
                if (!isClientWorld(entity) || (entity instanceof EntityPlayer)) {
                    entity.motionX *= 0.6;
                    entity.motionZ *= 0.6;
                }
            }
        });
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    @Override
    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public double getDoT(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.dot, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalNegativeEffect);
        return (result * getEffectiveness());
    }

    public double getDamage(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.damage, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalNegativeEffect);
        return (result * getEffectiveness());
    }

    public int getHoleDuration(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.holeDuration, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalTime);
        return (int) (result * getEffectiveness());
    }

    public float getHoleRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.holeRange, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalRange);
        return (float) (result * getEffectiveness());
    }

    public double getRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().range, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalRange);
        return (result * getEffectiveness());
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
            if (c.owns(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add("Hold SHIFT for stats.");
                } else {
                    c.get(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add("Endurance Drain: " + ModAttributes.ENDURANCE.getEnduranceDrain(this));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add("Max Level:");
                        } else {
                            description.add("Current Level:");
                        }
                        description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D) + "s");
                        description.add("Distance: " + TextHelper.format2FloatPoint(getRange(abilityInfo)) + " Blocks");
                        description.add("Black Hole Duration: " + TextHelper.format2FloatPoint(getHoleDuration(abilityInfo) / 20D) + "s");
                        description.add("Black Hole Range: " + TextHelper.format2FloatPoint(getHoleRange(abilityInfo)) + " Blocks");
                        description.add("Initial Damage: " + TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D) + " Hearts");
                        description.add("Duration: " + TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D) + "s");
                        description.add("DoT: " + TextHelper.format2FloatPoint(getDoT(abilityInfo) / 2D) + " Hearts");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D) + "s");
                            description.add("Distance: " + TextHelper.format2FloatPoint(getRange(infoNew)) + " Blocks");
                            description.add("Black Hole Duration: " + TextHelper.format2FloatPoint(getHoleDuration(infoNew) / 20D) + "s");
                            description.add("Black Hole Range: " + TextHelper.format2FloatPoint(getHoleRange(infoNew)) + " Blocks");
                            description.add("Initial Damage: " + TextHelper.format2FloatPoint(getDamage(infoNew) / 2D) + " Hearts");
                            description.add("Duration: " + TextHelper.format2FloatPoint(getTime(infoNew) / 20D) + "s");
                            description.add("DoT: " + TextHelper.format2FloatPoint(getDoT(infoNew) / 2D) + " Hearts");
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean canUpgrade(EntityLivingBase entity) {
        return Capabilities.advancement(entity).map(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            return c.level >= tokens && c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            if (c.level >= tokens && c.getExperienceTotal(entity) >= xp) {
                //c.tokensLevel -= tokens;
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public Requirement getRequirement(EntityLivingBase entity) {
        AbilityInfo info = (AbilityInfo) Capabilities.get(entity).flatMap(a -> a.get(this)).orElse(null);
        int tokensNeeded = 0;
        int xpNeeded;
        if (info == null) {
            int abilities = Capabilities.get(entity).map(c -> (int) c.getAll().keySet().stream().filter(s -> s instanceof BaseAbility).count()).orElse(0);
            if (abilities > 0) {
                tokensNeeded = abilities + 1;
            } else {
                tokensNeeded = 1;
            }
        }
        xpNeeded = getUpgradeCost(info);
        return new DefaultRequirement(tokensNeeded, getCostIncrement(entity, xpNeeded));
    }

    public int getCostIncrement(EntityLivingBase entity, int total) {
        Optional<AdvancementCapability> optional = Capabilities.advancement(entity);
        if (optional.isPresent()) {
            AdvancementCapability advancement = optional.get();
            List<Skill> skillUnlockOrder = Arrays.asList(advancement.skillUnlockOrder);
            int index = skillUnlockOrder.indexOf(ModAbilities.SHADOW);
            if (index == -1) {
                index = advancement.skillUnlockOrder.length;
            }
            return (int) (total * (1D + index * 0.5D));
        }
        return total;
    }

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
        Configuration.getSyncValues().range = Configuration.getValues().range;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().extra.holeDuration = Configuration.getValues().extra.holeDuration;
        Configuration.getSyncValues().extra.holeRange = Configuration.getValues().extra.holeRange;
        Configuration.getSyncValues().extra.dot = Configuration.getValues().extra.dot;
        Configuration.getSyncValues().extra.damage = Configuration.getValues().extra.damage;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "extra.holeDuration", Configuration.getValues().extra.holeDuration);
        NBTHelper.setArray(compound, "extra.holeRange", Configuration.getValues().extra.holeRange);
        NBTHelper.setArray(compound, "extra.dot", Configuration.getValues().extra.dot);
        NBTHelper.setArray(compound, "extra.damage", Configuration.getValues().extra.damage);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().range = NBTHelper.getArray(compound, "range");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.holeDuration = NBTHelper.getArray(compound, "extra.holeDuration");
        Configuration.getSyncValues().extra.holeRange = NBTHelper.getArray(compound, "extra.holeRange");
        Configuration.getSyncValues().extra.dot = NBTHelper.getArray(compound, "extra.dot");
        Configuration.getSyncValues().extra.damage = NBTHelper.getArray(compound, "extra.damage");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.BLACK_HOLE)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.BLACK_HOLE)
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
            public int maxLevel = 100;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){105 * 20 + 15 * 20 * (1 - ((1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))))}",
                    "(50+){100 * 20 + 5 * 20 * (1- (((e^(0.1 * ((x-49) / (y-49))) - 1)/((e^0.1) - 1))))}",
                    "(100){90 * 20}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){(8 * 20)}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){5 + 4 * (1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))}",
                    "(50+){9 + 2 * ((e^(0.1 * ((x - 49) / (y - 49))) - 1)/((e^0.1) - 1))}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Black Hole Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] holeDuration = {
                        "(0+){5 * 20}"
                };
                @Config.Comment("Black Hole Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] holeRange = {
                        "(0+){4 + 3 * (1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))}",
                        "(50+){7 + 1 * ((e^(0.1 * ((x - 49) / (y - 49))) - 1)/((e^0.1) - 1))}"
                };
                @Config.Comment("Damage Over Time Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] dot = {
                        "(0+){18 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (21.62 - 18)}",
                        "(50+){21.62 + ((e^(2.25 * ((x-49) / (y-49))) - 1)/((e^2.25) - 1)) * (28 - 21.62)}",
                        "(100){32}"
                };
                @Config.Comment("Initial Damage Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] damage = {
                        "(0+){26 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (32.22 - 26)}",
                        "(50+){32.22 + ((e^(3.25 * ((x-49) / (y-49))) - 1)/((e^3.25) - 1)) * (45 - 32.22)}",
                        "(100){50}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){22070}",
                        "(1+){7 * x}",
                        "(100){7 * x + 7 * x * 0.1}"
                };
            }
        }
    }
}
