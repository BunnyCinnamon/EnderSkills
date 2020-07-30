package arekkuusu.enderskills.common.skill.ability.offence.wind;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.*;
import arekkuusu.enderskills.api.capability.data.nbt.UUIDWatcher;
import arekkuusu.enderskills.api.event.SkillShouldUseEvent;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.AIOverride;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Pull extends BaseAbility implements IImpact, ISkillAdvancement {

    public Pull() {
        super(LibNames.PULL);
        setTexture(ResourceLibrary.PULL);
        MinecraftForge.EVENT_BUS.register(this);
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
            int time = getTime(abilityInfo);
            Vec3d lookVector = user.getLook(1F);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            NBTHelper.setVector(compound, "origin", user.getPositionVector());
            NBTHelper.setVector(compound, "vector", lookVector);
            SkillData data = SkillData.of(this)
                    .with(time + 10)
                    .put(compound, UUIDWatcher.INSTANCE)
                    .overrides(this)
                    .create();
            EntityThrowableData.throwFor(user, distance, data, false);
            sync(user);

            if (user.world instanceof WorldServer) {
                ((WorldServer) user.world).playSound(null, user.posX, user.posY, user.posZ, ModSounds.PULL, SoundCategory.PLAYERS, 1.0F, (1.0F + (user.world.rand.nextFloat() - user.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit instanceof EntityLivingBase && owner != null) {
            Vec3d positionVector = NBTHelper.getVector(skillData.nbt, "origin");
            Vec3d lookVector = NBTHelper.getVector(skillData.nbt, "vector");
            Vec3d targetVector = positionVector.addVector(
                    lookVector.x * 2,
                    lookVector.y * 2,
                    lookVector.z * 2
            );
            NBTHelper.setDouble(skillData.nbt, "force", trace.entityHit.getDistance(targetVector.x, targetVector.y, targetVector.z));
            apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);

            if (source.world instanceof WorldServer) {
                ((WorldServer) source.world).playSound(null, source.posX, source.posY, source.posZ, ModSounds.WIND_ON_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (source.world.rand.nextFloat() - source.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        if (tick > 10 || data.time == 10) {
            if (target instanceof EntityLiving) {
                ((EntityLiving) target).getNavigator().clearPath();
                ((EntityLiving) target).tasks.addTask(0, AIOverride.INSTANCE);
            }
            target.getEntityData().setBoolean("enderskills:stun_indicator", true);
        } else {
            Vec3d vector = NBTHelper.getVector(data.nbt, "vector").scale(-1);
            double distance = NBTHelper.getDouble(data.nbt, "force");
            Vec3d from = target.getPositionVector();
            Vec3d to = from.addVector(
                    vector.x * distance,
                    vector.y * distance,
                    vector.z * distance
            );
            moveEntity(to, from, target);
            if (target.collidedHorizontally) {
                target.motionY = 0;
            }
            RayTraceResult trace = RayTraceHelper.forwardsRaycast(target, true, true, target);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit instanceof EntityLivingBase) {
                Entity user = NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "user");
                if (trace.entityHit != user && !SkillHelper.isActiveNotOwner(trace.entityHit, this)) {
                    SkillHolder holder = new SkillHolder(data.copy());
                    holder.tick = tick;
                    Capabilities.get(trace.entityHit).ifPresent(skills -> skills.activate(holder)); //Add to entity Server Side
                    PacketHelper.sendSkillHolderUseResponsePacket((EntityLivingBase) trace.entityHit, holder);
                }
            }
        }
    }

    @Override
    public void end(EntityLivingBase target, SkillData data) {
        if (isClientWorld(target)) return;
        if (target instanceof EntityLiving) {
            ((EntityLiving) target).tasks.removeTask(AIOverride.INSTANCE);
        }
        target.getEntityData().setBoolean("enderskills:stun_indicator", false);
    }

    public void moveEntity(Vec3d pullerPos, Vec3d pushedPos, Entity pulled) {
        Vec3d distance = pullerPos.subtract(pushedPos);
        Vec3d motion = new Vec3d(distance.x / 10D, distance.y / 10D, distance.z / 10D).scale(-1);
        pulled.motionX = -motion.x;
        pulled.motionY = -motion.y;
        pulled.motionZ = -motion.z;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void inputListener(InputUpdateEvent event) {
        Capabilities.get(event.getEntityLiving()).flatMap(c -> c.getActive(this)).ifPresent(holder -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                if (event.getEntityLiving() != user) {
                    event.getMovementInput().forwardKeyDown = false;
                    event.getMovementInput().rightKeyDown = false;
                    event.getMovementInput().backKeyDown = false;
                    event.getMovementInput().leftKeyDown = false;
                    event.getMovementInput().sneak = false;
                    event.getMovementInput().jump = false;
                    event.getMovementInput().moveForward = 0;
                    event.getMovementInput().moveStrafe = 0;
                }
            });
        });
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillShouldUseEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        Capabilities.get(event.getEntityLiving()).flatMap(c -> c.getActive(this)).ifPresent(holder -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                if (event.getEntityLiving() != user) {
                    event.setCanceled(true);
                }
            });
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseClick(InputEvent.MouseInputEvent event) {
        Capabilities.get(Minecraft.getMinecraft().player).flatMap(c -> c.getActive(this)).ifPresent(holder -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                if (Minecraft.getMinecraft().player != user) {
                    if (Minecraft.getMinecraft().gameSettings.keyBindAttack.isPressed()) {
                        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                    }
                }
            });
        });
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
                        description.add("Stun Duration: " + TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D) + "s");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D) + "s");
                            description.add("Range: " + TextHelper.format2FloatPoint(getRange(infoNew)) + " Blocks");
                            description.add("Stun Duration: " + TextHelper.format2FloatPoint(getTime(infoNew) / 20D) + "s");
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
            int index = skillUnlockOrder.indexOf(ModAbilities.SLASH);
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
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
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
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.PULL)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.PULL)
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

            @Config.Comment("Max level obtainable")
            @Config.RangeInt(min = 0)
            public int maxLevel = 100;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){10.5 * 20 + 3.5 * 20 * (1 - ((1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))))}",
                    "(50+){9 * 20 + 1.5 * 20 * (1- (((e^(0.1 * ((x-49) / (y-49))) - 1)/((e^0.1) - 1))))}",
                    "(100){(8 * 20)}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){3 * 20 + 3 * 20 * (1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))}",
                    "(50+){7 * 20 + 1 * 20 * ((e^(0.1 * ((x - 49) / (y - 49))) - 1)/((e^0.1) - 1))}",
                    "(100){8 * 20}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){14 + 7 * (1 - (e^(-2.1 * (x/49)))) / (1 - e^(-2.1))}",
                    "(50+){21 + 3 * ((e^(0.1 * ((x - 49) / (y - 49))) - 1)/((e^0.1) - 1))}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

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
