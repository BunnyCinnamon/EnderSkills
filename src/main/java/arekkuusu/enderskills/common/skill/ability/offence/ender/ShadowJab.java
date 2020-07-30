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
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.sounds.ShadowJabSound;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class ShadowJab extends BaseAbility implements ISkillAdvancement {

    public ShadowJab() {
        super(LibNames.SHADOW_JAB);
        setTexture(ResourceLibrary.SHADOW_JAB);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase user, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(user)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (shouldUse(user) && canUse(user)) {
            if (!(user instanceof EntityPlayer) || !((EntityPlayer) user).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            NBTHelper.setNBT(compound, "list", new NBTTagList());
            NBTHelper.setDouble(compound, "dot", getDoT(abilityInfo));
            NBTHelper.setInteger(compound, "time", getTime(abilityInfo));
            NBTHelper.setDouble(compound, "range", getRange(abilityInfo));
            NBTHelper.setDouble(compound, "damage", getDamage(abilityInfo));
            SkillData data = SkillData.of(this)
                    .with(getTime(abilityInfo))
                    .put(compound, UUIDWatcher.INSTANCE)
                    .overrides(this)
                    .create();
            apply(user, data);
            sync(user, data);
            sync(user);
        }
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "user")).ifPresent(user -> {
                if (entity == user) {
                    makeSound(entity);
                }
            });
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new ShadowJabSound(entity));
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "user")).ifPresent(user -> {
            if (!isClientWorld(entity)) {
                if (entity == user) {
                    NBTHelper.getNBTList(data.nbt, "list").ifPresent(list -> {
                        double range = data.nbt.getDouble("range");
                        List<UUID> uuids = new ArrayList<>();
                        list.iterator().forEachRemaining(nbt -> {
                            uuids.add(((NBTTagCompound) nbt).getUniqueId("uuid"));
                        });
                        RayTraceHelper.getEntitiesInCone(user, range, 40, TeamHelper.SELECTOR_ENEMY.apply(user)).forEach(target -> {
                            if (target instanceof EntityLivingBase) {
                                if (!uuids.contains(target.getUniqueID())) {
                                    //Apply initial damage
                                    double damage = data.nbt.getDouble("damage");
                                    SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, user);
                                    SkillDamageEvent event = new SkillDamageEvent(user, this, source, damage);
                                    MinecraftForge.EVENT_BUS.post(event);
                                    target.attackEntityFrom(source, event.toFloat());
                                    // Add entity to exception list
                                    NBTTagCompound nbt = new NBTTagCompound();
                                    nbt.setUniqueId("uuid", target.getUniqueID());
                                    list.appendTag(nbt);

                                    if (user.world instanceof WorldServer) {
                                        ((WorldServer) user.world).playSound(null, user.posX, user.posY, user.posZ, ModSounds.VOID_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (user.world.rand.nextFloat() - user.world.rand.nextFloat()) * 0.2F) * 0.7F);
                                    }
                                }
                                //Remove effect
                                if (isActiveOwner(user, (EntityLivingBase) target)) {
                                    unapply((EntityLivingBase) target, data);
                                    async((EntityLivingBase) target, data);
                                }
                                //Add effect
                                SkillData override = SkillData.of(this)
                                        .with(data.nbt.getInteger("time"))
                                        .put(data.nbt, data.watcher.copy())
                                        .create();
                                apply((EntityLivingBase) target, override);
                                sync((EntityLivingBase) target, override);
                            }
                        });
                    });
                } else {
                    double damage = data.nbt.getDouble("dot");
                    double time = data.nbt.getInteger("time");
                    SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, user);
                    SkillDamageEvent event = new SkillDamageEvent(user, this, source, damage);
                    MinecraftForge.EVENT_BUS.post(event);
                    entity.attackEntityFrom(source, (float) (event.toFloat() / time));
                }
            }
            if (!isClientWorld(entity) || (entity instanceof EntityPlayer) && entity != user) {
                entity.motionX *= 0.6;
                entity.motionZ *= 0.6;
            }
        });
    }

    public boolean isActiveOwner(EntityLivingBase attacker, EntityLivingBase target) {
        return SkillHelper.isActive(target, this, h -> Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, h.data.nbt, "user")).map(e -> e == attacker).orElse(false));
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
                        description.add("Range: " + TextHelper.format2FloatPoint(getRange(abilityInfo)) + " Blocks");
                        description.add("Duration: " + TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D) + "s");
                        description.add("Initial Damage: " + TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D) + " Hearts");
                        description.add("DoT: " + TextHelper.format2FloatPoint(getDoT(abilityInfo) / 2D) + " Hearts");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D) + "s");
                            description.add("Range: " + TextHelper.format2FloatPoint(getRange(infoNew)) + " Blocks");
                            description.add("Duration: " + TextHelper.format2FloatPoint(getTime(infoNew) / 20D) + "s");
                            description.add("Initial Damage: " + TextHelper.format2FloatPoint(getDamage(infoNew) / 2D) + " Hearts");
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
        Configuration.getSyncValues().extra.dot = Configuration.getValues().extra.dot;
        Configuration.getSyncValues().extra.damage = Configuration.getValues().extra.dot;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
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
        Configuration.getSyncValues().extra.dot = NBTHelper.getArray(compound, "extra.dot");
        Configuration.getSyncValues().extra.damage = NBTHelper.getArray(compound, "extra.damage");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.SHADOW_JAB)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.SHADOW_JAB)
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
                    "(0+){17 * 20 + 11 * 20 * (1 - ((1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))))}",
                    "(50+){12 * 20 + 5 * 20 * (1- (((e^(0.1 * ((x-49) / (y-49))) - 1)/((e^0.1) - 1))))}",
                    "(100){(8 * 20)}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){10}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){2}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Damage Over Time Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] dot = {
                        "(0+){2 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (7.18 - 2)}",
                        "(50+){7.18 + ((e^(2.25 * ((x-49) / (y-49))) - 1)/((e^2.25) - 1)) * (21 - 7.18)}",
                        "(100){22}"
                };
                @Config.Comment("Initial Damage Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] damage = {
                        "(0+){4 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (7.62 - 4)}",
                        "(50+){7.11 + ((e^(2.25 * ((x-49) / (y-49))) - 1)/((e^2.25) - 1)) * (15 - 7.11)}",
                        "(100){16}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){825}",
                        "(1+){7 * x}",
                        "(100){7 * x + 7 * x * 0.1}"
                };
            }
        }
    }
}
