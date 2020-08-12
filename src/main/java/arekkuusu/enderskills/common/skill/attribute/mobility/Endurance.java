package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.event.SkillsActionableEvent;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.deffense.DamageResistance;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
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
                    Capabilities.endurance(entity).ifPresent(cap -> {
                        int amount = cap.getEnduranceDefault() + getModifier(attributeInfo);
                        if (amount != cap.getEnduranceMax()) {
                            if (cap.getEndurance() > amount) {
                                cap.setEndurance(amount);
                            }
                            cap.setEnduranceMax(amount);
                            if (entity instanceof EntityPlayerMP) {
                                PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                            }
                        }
                    });
                });
            } else {
                Capabilities.endurance(entity).ifPresent(cap -> {
                    if (cap.getEndurance() > cap.getEnduranceDefault()) {
                        cap.setEndurance(cap.getEnduranceDefault());
                    }
                    cap.setEnduranceMax(cap.getEnduranceDefault());
                    if (entity instanceof EntityPlayerMP) {
                        PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                    }
                });
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnduranceTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer || !event.getEntityLiving().getEntityWorld().isRemote) {
            Capabilities.endurance(event.getEntityLiving()).ifPresent(capability -> {
                if (capability.getEnduranceDelay() > 0) {
                    capability.setEnduranceDelay(capability.getEnduranceDelay() - 1);
                } else if (capability.getEndurance() < capability.getEnduranceMax()) {
                    capability.setEnduranceDelay(10); //Every half a second
                    capability.setEndurance(capability.getEndurance() + 1);
                }
            });
        }
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillsActionableEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            if (hasEnduranceDrain(event.getSkill())) {
                int enduranceNeeded = getEnduranceDrain(event.getSkill());
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                    enduranceNeeded = 0;
                }
                if (capability.getEndurance() < enduranceNeeded) {
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
                if (capability.getEndurance() < enduranceNeeded) {
                    event.setCanceled(true);
                    return;
                }
                capability.setEndurance(capability.getEndurance() - enduranceNeeded);
                capability.setEnduranceDelay(5 * 20);
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
                    description.add("Hold SHIFT for stats.");
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        description.clear();
                        if (attributeInfo.getLevel() >= getMaxLevel()) {
                            description.add("Max Level:");
                        } else {
                            description.add("Current Level:");
                        }
                        description.add("Boost: +" + getModifier(attributeInfo) + " Endurance");
                        if (attributeInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Boost: +" + getModifier(infoNew) + " Endurance");
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
        Configuration.getSyncValues().extra.enduranceMap = new HashMap<>(Configuration.getValues().extra.enduranceMap);
        DamageResistance.Configuration.getSyncValues().advancement.upgrade = DamageResistance.Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "modifier", Configuration.getValues().modifier);
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
