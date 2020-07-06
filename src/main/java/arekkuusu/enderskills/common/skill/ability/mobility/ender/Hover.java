package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.IInfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.nbt.UUIDWatcher;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.sounds.HoverSound;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Hover extends BaseAbility implements ISkillAdvancement {

    public Hover() {
        super(LibNames.HOVER);
        setTexture(ResourceLibrary.HOVER);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase user, SkillInfo skillInfo) {
        if (shouldUse(user) && canUse(user)) {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            int maxHover = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            SkillData data = SkillData.of(this)
                    .with(maxHover)
                    .put(compound, UUIDWatcher.INSTANCE)
                    .overrides(this)
                    .create();
            apply(user, data);
            sync(user, data);
            sync(user);
        }
    }

    @Override
    public void begin(EntityLivingBase user, SkillData data) {
        if (isClientWorld(user) && !(user instanceof EntityPlayer)) return;
        user.fallDistance = 0;
        if (isClientWorld(user)) {
            makeSound(user);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new HoverSound(entity));
    }

    @Override
    public void update(EntityLivingBase user, SkillData data, int tick) {
        if (isClientWorld(user) && !(user instanceof EntityPlayer)) return;
        if (user.motionY < 0) user.motionY *= 0.4D;
    }

    @SideOnly(Side.CLIENT)
    public static int ticksSinceLastTap;
    @SideOnly(Side.CLIENT)
    public static int hoverTime;
    @SideOnly(Side.CLIENT)
    public static boolean hovering;
    @SideOnly(Side.CLIENT)
    public static boolean canHover;

    static { //... really?
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            canHover = true;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player.capabilities.isCreativeMode) return;
        Capabilities.get(player).flatMap(c -> c.get(this)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            int maxHover = getTime(abilityInfo);
            if (maxHover <= 0) return;

            boolean pressed = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed();
            if (tapped && !player.onGround) {
                if (ticksSinceLastTap <= 7 && canHover) {
                    hovering = true;
                    canHover = false;
                } else {
                    ticksSinceLastTap = 0;
                }
            }
            if (!pressed && hovering) {
                PacketHelper.sendSkillRemoveResponsePacket(player, this);
                hovering = false;
                hoverTime = 0;
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (ticksSinceLastTap < 10) ticksSinceLastTap++;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && player.onGround && !canHover) canHover = true;
        if (player == null || !hovering) return;
        if (player.onGround) {
            PacketHelper.sendSkillRemoveResponsePacket(player, this);
            hovering = false;
            hoverTime = 0;
            return;
        }
        Capabilities.get(player).flatMap(c -> c.get(this)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            int maxHover = getTime(abilityInfo);
            if (maxHover <= 0) return;
            if (hoverTime > maxHover) {
                PacketHelper.sendSkillRemoveResponsePacket(player, this);
                hovering = false;
                hoverTime = 0;
            } else {
                Capabilities.endurance(player).ifPresent(endurance -> {
                    int amount = ModAttributes.ENDURANCE.getEnduranceDrain(this);
                    if (endurance.getEndurance() - amount >= 0) {
                        if (hovering) {
                            if (hoverTime++ == 0) {
                                PacketHelper.sendSkillUseRequestPacket(player, this);
                            }
                        }
                    } else {
                        PacketHelper.sendSkillRemoveResponsePacket(player, this);
                        hovering = false;
                        hoverTime = 0;
                    }
                });
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

    @Override
    public int getCooldown(AbilityInfo info) {
        return 0;
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

    @Override
    public boolean isKeyBound() {
        return false;
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
                        description.add("Duration: " + TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D) + "s");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Duration: " + TextHelper.format2FloatPoint(getTime(infoNew) / 20D) + "s");
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
            int index = skillUnlockOrder.indexOf(ModAbilities.WARP);
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
        Configuration.getSyncValues().time = Configuration.getValues().time;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.HOVER)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.HOVER)
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
            public int maxLevel = 5;

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {"(0+){(1 * 20) + ((1 * 20 * (x + 1)) - 1 * 20)}"};

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {"(0+){(825 * (1 - (0 ^ (0 ^ x)))) + 7 * x}"};
            }
        }
    }
}
