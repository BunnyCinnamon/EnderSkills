package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.client.sounds.SacrificeSound;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Sacrifice extends BaseAbility implements ISkillAdvancement {

    public Sacrifice() {
        super(LibNames.SACRIFICE, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            float power = getPower(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setFloat(compound, "power", power);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(getTime(abilityInfo))
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            apply(owner, data);
            sync(owner, data);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.SACRIFICE, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new SacrificeSound(entity));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getAmount() <= 0) return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        SkillHelper.getActiveFrom(attacker, this).ifPresent(data -> {
            float power = NBTHelper.getFloat(data.nbt, "power");
            event.setAmount(event.getAmount() + (event.getAmount() * power));
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || !SkillHelper.isSkillDamage(event.getSource())) return;
        EntityLivingBase entity = event.getEntityLiving();
        SkillHelper.getActiveFrom(entity, this).ifPresent(data -> {
            float power = NBTHelper.getFloat(data.nbt, "power");
            event.setAmount(event.getAmount() + (event.getAmount() * power));
        });
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public float getHealth(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.health, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalPositiveEffect);
        return (float) (result * getEffectiveness());
    }

    public float getPower(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.power, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalPositiveEffect);
        return (float) (result * getEffectiveness());
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

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
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
                            description.add(TextHelper.translate("desc.stats.level_max"));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current"));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getPower(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next"));
                            description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getPower(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
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
            int index = skillUnlockOrder.indexOf(ModAbilities.BLEED);
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
        Configuration.getSyncValues().extra.health = Configuration.getValues().extra.health;
        Configuration.getSyncValues().extra.power = Configuration.getValues().extra.power;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "extra.health", Configuration.getValues().extra.health);
        NBTHelper.setArray(compound, "extra.power", Configuration.getValues().extra.power);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.health = NBTHelper.getArray(compound, "extra.health");
        Configuration.getSyncValues().extra.power = NBTHelper.getArray(compound, "extra.power");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.SACRIFICE)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.SACRIFICE)
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
            public int maxLevel = 4;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){102 * 20 + 18 * 20 * (1 - ((1 - (e^(-2.1 * (x/1)))) / (1 - e^(-2.1))))}",
                    "(2+){94 * 20 + 8 * 20 * (1- (((e^(0.1 * ((x-1) / (y-1))) - 1)/((e^0.1) - 1))))}",
                    "(4){(90 * 20)}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){25 * 20 + 13 * 20 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                    "(25+){38 * 20 + 5 * 20 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}",
                    "(50){45 * 20}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Health Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] health = {
                        "(0+){0.05 + x * 0.05}"
                };
                @Config.Comment("Power Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] power = {
                        "(0+){2 + x * 0.05}"
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
