package arekkuusu.enderskills.common.skill.ability.defense.fire;

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
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.sounds.OverheatSound;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Overheat extends BaseAbility implements ISkillAdvancement {

    public Overheat() {
        super(LibNames.OVERHEAT, new AbilityProperties());
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
            int time = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(time)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            apply(owner, data);
            sync(owner, data);
            sync(owner);
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
        Minecraft.getMinecraft().getSoundHandler().playSound(new OverheatSound(entity));
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if(isClientWorld(entity)) {
            float particleProgress = Math.min(((float) tick / ((float) data.time / 3F)), 1F);
            for (int i = 0; i < 3; i++) {
                if (entity.world.rand.nextDouble() < 0.9D * particleProgress && ClientProxy.canParticleSpawn()) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                    double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
                    double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                    float particleScale = 0.25F + 2F * particleProgress;
                    Vector speedVec = Vector.Up.rotateRandom(entity.world.rand, 70F).multiply(0.15D * entity.world.rand.nextDouble());
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), speedVec.toVec3d(), particleScale, 30, ResourceLibrary.GLOW);
                    if(ClientProxy.canParticleSpawn()) {
                        double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25D;
                        double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25D;
                        entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, motionX, 0.01D, motionZ);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || event.getSkill() != ModEffects.BURNING) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AbilityInfo attributeInfo = (AbilityInfo) skillInfo;
                    if (Configuration.getSyncValues().extra.multiplyDamage) {
                        event.setAmount(event.getAmount() + event.getAmount() * getModifierMultiplication(attributeInfo));
                    } else {
                        event.setAmount(event.getAmount() + getModifierAddition(attributeInfo));
                    }
                });
            }
        });
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public int getModifierAddition(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.modifier, level, levelMax);
        return (int) (func * getEffectiveness());
    }

    public float getModifierMultiplication(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.modifier, level, levelMax);
        return (float) (func * getEffectiveness());
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
                        if (Configuration.getSyncValues().extra.multiplyDamage) {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifierMultiplication(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        } else {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifierAddition(abilityInfo))));
                        }
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            if (Configuration.getSyncValues().extra.multiplyDamage) {
                                description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifierMultiplication(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            } else {
                                description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifierAddition(infoNew))));
                            }
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
            int index = skillUnlockOrder.indexOf(ModAbilities.FLARES);
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
        Configuration.getSyncValues().extra.multiplyDamage = Configuration.getValues().extra.multiplyDamage;
        Configuration.getSyncValues().extra.modifier = Configuration.getValues().extra.modifier;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setBoolean(compound, "extra.multiplyDamage", Configuration.getValues().extra.multiplyDamage);
        NBTHelper.setArray(compound, "extra.modifier", Configuration.getValues().extra.modifier);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.multiplyDamage = NBTHelper.getBoolean(compound, "extra.multiplyDamage");
        Configuration.getSyncValues().extra.modifier = NBTHelper.getArray(compound, "extra.modifier");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.OVERHEAT)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.OVERHEAT)
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
                    "(0+){50 * 20 + 20 * 20 * (1 - ((1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))))}",
                    "(25+){40 * 20 + 10 * 20 * (1- (((e^(0.1 * ((x-24) / (y-24))) - 1)/((e^0.1) - 1))))}",
                    "(50){36 * 20}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){10 * 20}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("This config option when active will make Damage multiply the total damage instead of adding a flat amount")
                public boolean multiplyDamage = true;
                @Config.Comment("Modifier Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] modifier = {
                        "(0+){2}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){600}",
                        "(1+){4 * x}",
                        "(50){4 * x + 4 * x * 0.1}"
                };
            }
        }
    }
}
