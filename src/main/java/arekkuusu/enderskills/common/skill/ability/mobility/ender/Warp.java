package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.IInfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.nbt.UUIDWatcher;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
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
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
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

public class Warp extends BaseAbility implements ISkillAdvancement {

    public Warp() {
        super(LibNames.WARP);
        setTexture(ResourceLibrary.WARP);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void use(EntityLivingBase user, SkillInfo skillInfo, Vec3d vector) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(user)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        Vec3d eyesVector = user.getPositionEyes(1F);
        Vec3d targetVector = eyesVector.add(vector);
        RayTraceResult traceBlocks = RayTraceHelper.rayTraceBlocksExcept(user.world, eyesVector, targetVector);
        if (traceBlocks != null) {
            targetVector = traceBlocks.hitVec;
            targetVector = new Vec3d(
                    MathHelper.floor(targetVector.x) + 0.5D,
                    MathHelper.floor(targetVector.y) + 0.1D,
                    MathHelper.floor(targetVector.z) + 0.5D
            );
        }
        double distance = getRange(abilityInfo);
        if (user.getDistance(targetVector.x, targetVector.y, targetVector.z) > distance + 1) return; //Cheater...

        BlockPos posFloor = new BlockPos(targetVector); //One Bwock fwom bwock tawgeted
        BlockPos posCeiling = posFloor.up(); //One spawce up fow youw wittle head uwu
        if (isSafePos(user.world, posFloor) && isSafePos(user.world, posCeiling) && shouldUse(user) && canUse(user)) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            NBTHelper.setVector(compound, "origin", user.getPositionVector());
            NBTHelper.setVector(compound, "target", targetVector);
            if (!(user instanceof EntityPlayer) || !((EntityPlayer) user).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            SkillData data = SkillData.of(this)
                    .with(INSTANT)
                    .put(compound, UUIDWatcher.INSTANCE)
                    .create();
            apply(user, data);
            sync(user, data);
            sync(user);
        }
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        Vec3d vec = NBTHelper.getVector(data.nbt, "target");
        entity.setPositionAndUpdate(vec.x, vec.y, vec.z);
        if (entity.world instanceof WorldServer) {
            ((WorldServer) entity.world).playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ, ModSounds.WARP, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
            ((WorldServer) entity.world).playSound(null, vec.x, vec.y, vec.z, ModSounds.WARP, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    public boolean isSafePos(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB;
    }

    @SideOnly(Side.CLIENT)
    public static int ticksSinceLastTap;
    @SideOnly(Side.CLIENT)
    public static boolean keyWasPressed;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (!Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) return;
        Capabilities.get(player).flatMap(c -> c.get(this)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown()) return;
            boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindSprint.isPressed();
            if (tapped) {
                //Pressed same combination within 7 ticks
                if (ticksSinceLastTap <= 7) {
                    Capabilities.endurance(player).ifPresent(endurance -> {
                        int amount = ModAttributes.ENDURANCE.getEnduranceDrain(this);
                        if (endurance.getEndurance() - amount >= 0) {
                            double distance = getRange(abilityInfo);
                            Vec3d lookVec = player.getLookVec();
                            double x = lookVec.x * distance;
                            double y = lookVec.y * distance;
                            double z = lookVec.z * distance;
                            Vec3d moveVec = new Vec3d(0, 0, 0);
                            if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(x, y, z));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-z, 0, x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(z, 0, -x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-x, -y, -z));
                            }
                            PacketHelper.sendWarpUseRequestPacket(player, moveVec);
                        }
                    });
                    keyWasPressed = true;
                } else {
                    ticksSinceLastTap = 0;
                }
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) keyWasPressed = false;
        if (ticksSinceLastTap < 10) ticksSinceLastTap++;
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    @Override
    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
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
                        description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D) + "s");
                        description.add("Range: " + TextHelper.format2FloatPoint(getRange(abilityInfo)) + " Blocks");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D) + "s");
                            description.add("Range: " + TextHelper.format2FloatPoint(getRange(infoNew)) + " Blocks");
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
                Capabilities.get(entity).filter(a -> !a.owns(this)).ifPresent(a -> {
                    Skill[] skillUnlockOrder = Arrays.copyOf(c.skillUnlockOrder, c.skillUnlockOrder.length + 1);
                    skillUnlockOrder[skillUnlockOrder.length - 1] = this;
                    c.skillUnlockOrder = skillUnlockOrder;
                });
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
        Configuration.getSyncValues().cooldown = Configuration.getValues().cooldown;
        Configuration.getSyncValues().range = Configuration.getValues().range;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().range = NBTHelper.getArray(compound, "range");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.WARP)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.WARP)
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
                    "(0+){(8 * 20) + (4 * 20) * (1 - ((e^(-0.1 * (x / y)) - 1)/((e^-0.1) - 1)))}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){4 + (x / y) * (8 - 4)}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0+){(170 * (1 - (0 ^ (0 ^ x)))) + 7 * x}"
                };
            }
        }
    }
}
