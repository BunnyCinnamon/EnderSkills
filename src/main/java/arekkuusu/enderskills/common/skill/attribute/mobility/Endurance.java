package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.deffense.DamageResistance;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Endurance extends BaseAttribute implements ISkillAdvancement {

    //Vanilla Attribute
    public static final IAttribute MAX_ENDURANCE = new RangedAttribute(null, "enderskills.generic.maxEndurance", 0F, 0F, Float.MAX_VALUE).setDescription("Max Endurance").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ENDURANCE_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84adc38600",
            LibMod.MOD_ID + ":" + LibNames.ENDURANCE,
            Endurance.MAX_ENDURANCE,
            Constants.AttributeModifierOperation.ADD);
    //Vanilla Attribute Modifier for default Endurance attribute
    public static final DynamicModifier ENDURANCE_DEFAULT_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84a3c38600",
            LibMod.MOD_ID + ":" + LibNames.ENDURANCE + "_default",
            Endurance.MAX_ENDURANCE,
            Constants.AttributeModifierOperation.ADD);

    public Endurance() {
        super(LibNames.ENDURANCE, new BaseProperties());
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
                    ENDURANCE_ATTRIBUTE.apply(entity, getModifier(attributeInfo));
                });
            } else {
                if (ENDURANCE_ATTRIBUTE.remove(entity)) {
                    Capabilities.endurance(entity).ifPresent(enduranceCapability -> {
                        double amount = entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
                        if (enduranceCapability.getEndurance() > amount) {
                            enduranceCapability.setEndurance(amount);
                            if (entity instanceof EntityPlayerMP) {
                                PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                            }
                        }
                    });
                }
            }
        });
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(MAX_ENDURANCE).setBaseValue(0F);
        }
    }

    @SubscribeEvent
    public void resetDefault(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof EntityLivingBase) {
            ((EntityLivingBase) event.getEntity()).getEntityAttribute(MAX_ENDURANCE).setBaseValue(0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnduranceTick(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            Capabilities.endurance(event.getEntityLiving()).ifPresent(capability -> {
                double maxEndurance = event.getEntityLiving().getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
                if (capability.getEnduranceDelay() > 0) {
                    capability.setEnduranceDelay(capability.getEnduranceDelay() - 1);
                } else if (capability.getEndurance() < maxEndurance) {
                    capability.setEnduranceDelay(10); //Every half a second
                    capability.setEndurance(Math.min(capability.getEndurance() + (maxEndurance / (maxEndurance - capability.getEndurance())), maxEndurance));
                } else if (capability.getEndurance() > maxEndurance) {
                    capability.setEnduranceDelay(10); //Every half a second
                    capability.setEndurance(Math.max(capability.getEndurance() - (capability.getEndurance() / maxEndurance), 0D));
                }
                if (event.getEntityLiving() instanceof EntityPlayerMP) {
                    PacketHelper.sendEnduranceSync((EntityPlayerMP) event.getEntityLiving());
                }
            });
            ENDURANCE_DEFAULT_ATTRIBUTE.apply(event.getEntityLiving(), Configuration.getSyncValues().endurance);
        }
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillActionableEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            if (hasEnduranceDrain(event.getSkill())) {
                int enduranceNeeded = getEnduranceDrain(event.getSkill());
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                    enduranceNeeded = 0;
                }
                if (capability.getEndurance() + capability.getAbsorption() < enduranceNeeded) {
                    event.setCanceled(true);
                }
            }
        });
    }

    @SubscribeEvent
    public void onSkillUse(SkillActivateEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            if (hasEnduranceDrain(event.getSkill())) {
                int enduranceNeeded = getEnduranceDrain(event.getSkill());
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                    enduranceNeeded = 0;
                }
                if (capability.getEndurance() + capability.getAbsorption() < enduranceNeeded) {
                    event.setCanceled(true);
                    return;
                }
                capability.drain(enduranceNeeded);
                if (entity instanceof EntityPlayerMP) {
                    PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                }
            }
        });
    }

    public boolean hasEnduranceDrain(Skill skill) {
        return getEnduranceDrain(skill) != 0;
    }

    public int getEnduranceDrain(Skill skill) {
        String skillRegistryName = Objects.requireNonNull(skill.getRegistryName()).toString();
        return Configuration.getSyncValues().extra.enduranceMap.get(skillRegistryName);
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public int getModifier(AttributeInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().modifier, level, levelMax);
        return (int) (func * getEffectiveness());
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
                            description.add(TextHelper.translate("desc.stats.level_current", attributeInfo.getLevel(), attributeInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(attributeInfo))));
                        if (attributeInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", attributeInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(infoNew))));
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
        Configuration.getSyncValues().endurance = Configuration.getValues().endurance;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().extra.enduranceMap = new HashMap<>(Configuration.getValues().extra.enduranceMap);
        DamageResistance.Configuration.getSyncValues().advancement.upgrade = DamageResistance.Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "modifier", Configuration.getValues().modifier);
        compound.setDouble("endurance", Configuration.getValues().endurance);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, Integer> entry : Configuration.getValues().extra.enduranceMap.entrySet()) {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTHelper.setString(nbt, "skill", entry.getKey());
            NBTHelper.setInteger(nbt, "cost", entry.getValue());
            list.appendTag(nbt);
        }
        compound.setTag("extra.enduranceMap", list);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().modifier = NBTHelper.getArray(compound, "modifier");
        Configuration.getSyncValues().endurance = compound.getDouble("endurance");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
        NBTTagList list = compound.getTagList("extra.enduranceMap", Constants.NBT.TAG_COMPOUND);
        Configuration.getSyncValues().extra.enduranceMap.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            String skill = nbt.getString("skill");
            Integer cost = nbt.getInteger("cost");
            Configuration.getSyncValues().extra.enduranceMap.put(skill, cost);
        }
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Attribute/" + LibNames.ENDURANCE)
    public static class Configuration {

        @Config.Comment("Attribute Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.ENDURANCE)
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

            @Config.Comment("The Maximum level of this Skill")
            @Config.RangeInt(min = 0)
            public int maxLevel = Integer.MAX_VALUE;

            @Config.Comment("Modifier Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] modifier = {
                    "(0+){x * 1}"
            };

            @Config.Comment("Default start endurance")
            public double endurance = 40D;

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Endurance drain by skill")
                public Map<String, Integer> enduranceMap = new HashMap<>(new ImmutableMap.Builder<String, Integer>()
                        //Defense-Light
                        .put(LibMod.MOD_ID + ":" + LibNames.CHARM, 6)
                        .put(LibMod.MOD_ID + ":" + LibNames.HEAL_AURA, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.POWER_BOOST, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.HEAL_OTHER, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.HEAL_SELF, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.NEARBY_INVINCIBILITY, 16)
                        //Defense-Earth
                        .put(LibMod.MOD_ID + ":" + LibNames.TAUNT, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.WALL, 9)
                        .put(LibMod.MOD_ID + ":" + LibNames.DOME, 9)
                        .put(LibMod.MOD_ID + ":" + LibNames.THORNY, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.SHOCKWAVE, 14)
                        .put(LibMod.MOD_ID + ":" + LibNames.ANIMATED_STONE_GOLEM, 16)
                        //Defense-Electric
                        .put(LibMod.MOD_ID + ":" + LibNames.SHOCKING_AURA, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.ELECTRIC_PULSE, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.MAGNETIC_PULL, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.POWER_DRAIN, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.ENERGIZE, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.VOLTAIC_SENTINEL, 16)
                        //Defense-Fire
                        .put(LibMod.MOD_ID + ":" + LibNames.FLARES, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.RING_OF_FIRE, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.BLAZING_AURA, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.OVERHEAT, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.WARM_HEART, 12)
                        .put(LibMod.MOD_ID + ":" + LibNames.HOME_STAR, 16)
                        //Mobility-Wind
                        .put(LibMod.MOD_ID + ":" + LibNames.DASH, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.EXTRA_JUMP, 2)
                        .put(LibMod.MOD_ID + ":" + LibNames.FOG, 16)
                        .put(LibMod.MOD_ID + ":" + LibNames.SMASH, 14)
                        .put(LibMod.MOD_ID + ":" + LibNames.HASTEN, 16)
                        .put(LibMod.MOD_ID + ":" + LibNames.SPEED_BOOST, 18)
                        //Mobility-Void
                        .put(LibMod.MOD_ID + ":" + LibNames.WARP, 6)
                        .put(LibMod.MOD_ID + ":" + LibNames.INVISIBILITY, 18)
                        .put(LibMod.MOD_ID + ":" + LibNames.HOVER, 2)
                        .put(LibMod.MOD_ID + ":" + LibNames.UNSTABLE_PORTAL, 14)
                        .put(LibMod.MOD_ID + ":" + LibNames.PORTAL, 16)
                        .put(LibMod.MOD_ID + ":" + LibNames.TELEPORT, 20)
                        //Offense-Void
                        .put(LibMod.MOD_ID + ":" + LibNames.SHADOW, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.GLOOM, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.SHADOW_JAB, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.GAS_CLOUD, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.GRASP, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.BLACK_HOLE, 18)
                        //Offense-Blood
                        .put(LibMod.MOD_ID + ":" + LibNames.BLEED, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.BLOOD_POOL, 6)
                        .put(LibMod.MOD_ID + ":" + LibNames.CONTAMINATE, 6)
                        .put(LibMod.MOD_ID + ":" + LibNames.LIFE_STEAL, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.SYPHON, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.SACRIFICE, 18)
                        //Offense-Wind
                        .put(LibMod.MOD_ID + ":" + LibNames.SLASH, 2)
                        .put(LibMod.MOD_ID + ":" + LibNames.PUSH, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.PULL, 4)
                        .put(LibMod.MOD_ID + ":" + LibNames.CRUSH, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.UPDRAFT, 6)
                        .put(LibMod.MOD_ID + ":" + LibNames.SUFFOCATE, 18)
                        //Offense-Fire
                        .put(LibMod.MOD_ID + ":" + LibNames.FIRE_SPIRIT, 1)
                        .put(LibMod.MOD_ID + ":" + LibNames.FLAMING_BREATH, 8)
                        .put(LibMod.MOD_ID + ":" + LibNames.FLAMING_RAIN, 10)
                        .put(LibMod.MOD_ID + ":" + LibNames.FOCUS_FLAME, 16)
                        .put(LibMod.MOD_ID + ":" + LibNames.FIREBALL, 16)
                        .put(LibMod.MOD_ID + ":" + LibNames.EXPLODE, 20)
                        .build()
                );
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0+){(50 * (1 - (0 ^ (0 ^ x)))) + 20 + 15 * x}"
                };
            }
        }
    }
}
