package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.deffense.DamageResistance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class Speed extends BaseAttribute implements ISkillAdvancement {

    public static final DynamicModifier SPEED_ATTRIBUTE = new DynamicModifier(
            "6f940b5d-107f-41bb-9217-1a34648fc5b7",
            LibMod.MOD_ID + ":" + LibNames.SPEED,
            SharedMonsterAttributes.MOVEMENT_SPEED,
            Constants.AttributeModifierOperation.ADD_MULTIPLE
    );

    public Speed() {
        super(LibNames.SPEED, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    SPEED_ATTRIBUTE.apply(entity, getModifier(attributeInfo));
                });
            } else {
                SPEED_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fovChanged(FOVUpdateEvent event) {
        if (event.getNewfov() > 1.1F) {
            event.setNewfov(1.1F);
        }
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public double getModifier(AttributeInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().modifier, level, levelMax);
        return (func * getEffectiveness());
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
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        description.clear();
                        if (attributeInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max"));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current"));
                        }
                        description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        if (attributeInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next"));
                            description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
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
            int xp = requirement.getXp();
            return c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            if (c.getExperienceTotal(entity) >= xp) {
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public Requirement getRequirement(EntityLivingBase entity) {
        AttributeInfo info = (AttributeInfo) Capabilities.get(entity).flatMap(a -> a.getOwned(this)).orElse(null);
        return new DefaultRequirement(0, getUpgradeCost(info));
    }

    public int getUpgradeCost(@Nullable AttributeInfo info) {
        int level = info != null ? getLevel(info) + 1 : 0;
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().advancement.upgrade, level, levelMax);
        return (int) (func * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }
    /*Advancement Section*/

    @Override
    public void initSyncConfig() {
        Configuration.getSyncValues().maxLevel = Configuration.getValues().maxLevel;
        Configuration.getSyncValues().modifier = Configuration.getValues().modifier;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        DamageResistance.Configuration.getSyncValues().advancement.upgrade = DamageResistance.Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "modifier", Configuration.getValues().modifier);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().modifier = NBTHelper.getArray(compound, "modifier");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Attribute/" + LibNames.SPEED)
    public static class Configuration {

        @Config.Comment("Attribute Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.SPEED)
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
            @Config.Comment("Skill specific Advancement Configuration")
            public final Advancement advancement = new Advancement();

            @Config.Comment("The Maximum level of this Skill")
            @Config.RangeInt(min = 0)
            public int maxLevel = 10;

            @Config.Comment("Modifier Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] modifier = {
                    "(0+){x * 0.01}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0+){(136 * (1 - (0 ^ (0 ^ x)))) + 400 * 2 * (x / y)}"
                };
            }
        }
    }
}
