package arekkuusu.enderskills.common.skill.ability.offence.fire;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.nbt.UUIDWatcher;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.ability.offence.ender.Gloom;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
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

public class Fireball extends BaseAbility implements IImpact, IScanEntities, IExpand, IFindEntity, ISkillAdvancement {

    public Fireball() {
        super(LibNames.FIREBALL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase user, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(user)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        double distance = getRange(abilityInfo);

        if (isActionable(user) && canActivate(user)) {
            if (!(user instanceof EntityPlayer) || !((EntityPlayer) user).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double range = getFlameRange(abilityInfo);
            int time = getFlameDuration(abilityInfo);
            double damage = getDamage(abilityInfo);
            int dotDuration = getTime(abilityInfo);
            double dot = getDoT(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, user, "user");
            NBTHelper.setDouble(compound, "damage", damage);
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setInteger(compound, "time", time);
            NBTHelper.setDouble(compound, "dot", dot);
            NBTHelper.setInteger(compound, "dotDuration", dotDuration);

            SkillData data = SkillData.of(this)
                    .with(dotDuration)
                    .put(compound, UUIDWatcher.INSTANCE)
                    .overrides(this)
                    .create();
            EntityThrowableData.throwFor(user, distance, data, false);
            sync(user);

            if (user.world instanceof WorldServer) {
                ((WorldServer) user.world).playSound(null, user.posX, user.posY, user.posZ, ModSounds.FIREBALL, SoundCategory.PLAYERS, 1.0F, (1.0F + (user.world.rand.nextFloat() - user.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        int time = skillData.nbt.getInteger("time");
        double radius = skillData.nbt.getDouble("range");
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, skillData, time + 5);
        Vec3d hitVector = trace.hitVec;
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
            hitVector = new Vec3d(hitVector.x, hitVector.y + trace.entityHit.getEyeHeight(), hitVector.z);
        }
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        spawn.growTicks = 5;
        source.world.spawnEntity(spawn); //MANIFEST B L O O D!!

        if (source.world instanceof WorldServer) {
            ((WorldServer) source.world).playSound(null, hitVector.x, hitVector.y, hitVector.z, ModSounds.FIREBALL_EXPLODE, SoundCategory.BLOCKS, 1.0F, (1.0F + (source.world.rand.nextFloat() - source.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    @Override
    public AxisAlignedBB expand(Entity source, @Nullable EntityLivingBase owner, AxisAlignedBB bb, float amount) {
        return bb.grow(amount * 2);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        apply(target, skillData);
        sync(target, skillData);

        if (source.world instanceof WorldServer) {
            ((WorldServer) source.world).playSound(null, source.posX, source.posY, source.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (source.world.rand.nextFloat() - source.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        SkillHelper.getOwner(data).ifPresent(user -> {
            double damage = data.nbt.getDouble("damage");
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, user);
            source.setExplosion();
            SkillDamageEvent event = new SkillDamageEvent(user, this, source, damage);
            MinecraftForge.EVENT_BUS.post(event);
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        });
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        SkillHelper.getOwner(data).ifPresent(user -> {
            double damage = data.nbt.getDouble("dot");
            double time = data.nbt.getInteger("dotDuration");
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, user);
            SkillDamageEvent event = new SkillDamageEvent(user, this, source, damage);
            MinecraftForge.EVENT_BUS.post(event);
            entity.attackEntityFrom(event.getSource(), (float) (event.toFloat() / time));
        });
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public float getFlameRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.flameRange, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalRange);
        return (float) (result * getEffectiveness());
    }

    public int getFlameDuration(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.flameDuration, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalTime);
        return (int) (result * getEffectiveness());
    }

    public double getDoT(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Gloom.Configuration.getSyncValues().extra.dot, level, levelMax);
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
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add("Hold SHIFT for stats.");
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
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
                        description.add("Explosion Size: " + TextHelper.format2FloatPoint(getFlameRange(abilityInfo)) + " Blocks");
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
                            description.add("Explosion Size: " + TextHelper.format2FloatPoint(getFlameRange(infoNew)) + " Blocks");
                            description.add("Initial Damage: " + TextHelper.format2FloatPoint(getDamage(infoNew) / 2D) + " Hearts");
                            description.add("DoT: " + TextHelper.format2FloatPoint(getDoT(infoNew) / 2D) + " Hearts");
                        }
                    });
                }
            }
        });
    }

    public int getCostIncrement(EntityLivingBase entity, int total) {
        Optional<AdvancementCapability> optional = Capabilities.advancement(entity);
        if (optional.isPresent()) {
            AdvancementCapability advancement = optional.get();
            List<Skill> skillUnlockOrder = Arrays.asList(advancement.skillUnlockOrder);
            int index = skillUnlockOrder.indexOf(ModAbilities.FIRE_SPIRIT);
            if (index == -1) {
                index = advancement.skillUnlockOrder.length;
            }
            return (int) (total * (1D + index * CommonConfig.getSyncValues().advancement.xp.costIncrement));
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
        Configuration.getSyncValues().extra.flameRange = Configuration.getValues().extra.flameRange;
        Configuration.getSyncValues().extra.flameDuration = Configuration.getValues().extra.flameDuration;
        Configuration.getSyncValues().extra.damage = Configuration.getValues().extra.damage;
        Configuration.getSyncValues().extra.dot = Configuration.getValues().extra.dot;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "extra.flameRange", Configuration.getValues().extra.flameRange);
        NBTHelper.setArray(compound, "extra.flameDuration", Configuration.getValues().extra.flameDuration);
        NBTHelper.setArray(compound, "extra.damage", Configuration.getValues().extra.damage);
        NBTHelper.setArray(compound, "extra.dot", Configuration.getValues().extra.dot);
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
        Configuration.getSyncValues().extra.flameRange = NBTHelper.getArray(compound, "extra.flameRange");
        Configuration.getSyncValues().extra.flameDuration = NBTHelper.getArray(compound, "extra.flameDuration");
        Configuration.getSyncValues().extra.damage = NBTHelper.getArray(compound, "extra.damage");
        Configuration.getSyncValues().extra.dot = NBTHelper.getArray(compound, "extra.dot");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.FIREBALL)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.FIREBALL)
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
                    "(0+){35 * 20 + 25 * 20 * (1 - ((1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))))}",
                    "(25+){25 * 20 + 10 * 20 * (1- (((e^(0.1 * ((x-24) / (y-24))) - 1)/((e^0.1) - 1))))}",
                    "(50){20 * 20}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){15 * 20}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){14 + 7 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                    "(25+){21 + 3 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Flame Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] flameRange = {
                        "(0+){3 + 1 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                        "(25+){4 + 1 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}"
                };
                @Config.Comment("Flame Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] flameDuration = {
                        "(0+){0}"
                };
                @Config.Comment("Damage Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] damage = {
                        "(0+){40 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (52.96 - 40)}",
                        "(25+){52.96 + ((e^(2.25 * ((x-24) / (y-24))) - 1)/((e^2.25) - 1)) * (86 - 52.96)}",
                        "(50){90}"
                };
                @Config.Comment("Damage Over Time Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] dot = {
                        "(0+){14 + ((e^(0.1 * (x / 49)) - 1)/((e^0.1) - 1)) * (20.73 - 14)}",
                        "(25+){20.73 + ((e^(2.25 * ((x-24) / (y-24))) - 1)/((e^2.25) - 1)) * (38 - 20.73)}",
                        "(50){40}"
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
