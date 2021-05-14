package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.sounds.RingOfFireSound;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.*;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableRingOfFire;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RingOfFire extends BaseAbility implements IScanEntities, IFindEntity, IExpand, IImpact, ILoopSound {

    public RingOfFire() {
        super(LibNames.RING_OF_FIRE, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (!((SkillInfo.IInfoCooldown) skillInfo).hasCooldown() && isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double range = getRange(abilityInfo);
            double ringRange = getRingRange(abilityInfo);
            double dot = getDoT(abilityInfo);
            int time = getTime(abilityInfo);
            int dotTime = getDoTTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setDouble(compound, "ringRange", ringRange);
            NBTHelper.setDouble(compound, "dot", dot);
            NBTHelper.setDouble(compound, "dotDuration", dotTime);
            NBTHelper.setInteger(compound, "time", time);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(INDEFINITE)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            EntityThrowableData.throwFor(owner, range, data, true);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit != RayTraceResult.Type.MISS) {
            Vec3d hitVector = trace.hitVec;

            EntityPlaceableRingOfFire spawn = new EntityPlaceableRingOfFire(owner.world, owner, skillData, skillData.nbt.getInteger("time"));
            spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
            spawn.setRadius(skillData.nbt.getDouble("ringRange"));
            spawn.spreadOnTerrain();
            owner.world.spawnEntity(spawn);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeSound(Entity source) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new RingOfFireSound((EntityPlaceableData) source));
    }

    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        List<Entity> entities = source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
        List<BlockPos> terrain = ((EntityPlaceableRingOfFire) source).getTerrainBlocks();
        entities.removeIf(entity -> {
            BlockPos entityPos = entity.getPosition();
            boolean withinHeight = false;
            for (int i = 1; i <= 2; i++) {
                BlockPos pos = entityPos.down(i);
                if (terrain.stream().anyMatch(p -> p.getX() == pos.getX() && p.getZ() == pos.getZ())) {
                    withinHeight = pos.getY() - entity.posY <= skillData.nbt.getDouble("ringRange");
                    break;
                }
            }
            return !withinHeight;
        });
        return entities;
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!MathUtil.fuzzyEqual(target.motionX, 0D)
                || !MathUtil.fuzzyEqual(target.motionY, 0D)
                || !MathUtil.fuzzyEqual(target.motionZ, 0D)) {
            List<BlockPos> terrain = ((EntityPlaceableRingOfFire) source).getTerrainBlocks();
            Vec3d posTarget = target.getPositionVector();
            Vec3d posSource = source.getPositionVector();

            boolean isBorder = false;
            BlockPos targetBlockPos = new BlockPos(posTarget);
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos nextToTarget = targetBlockPos.offset(facing);
                BlockPos perpendicularToTarget = nextToTarget.offset(facing.rotateY());
                boolean isCloseToNonTerrain = terrain.stream().noneMatch(pos -> pos.equals(nextToTarget)
                        || pos.down().equals(nextToTarget)
                        || pos.up().equals(nextToTarget)
                        || pos.equals(perpendicularToTarget)
                        || pos.down().equals(perpendicularToTarget)
                        || pos.up().equals(perpendicularToTarget)
                );
                if (isCloseToNonTerrain) {
                    isBorder = true;
                    break;
                }
            }

            if (isBorder) {
                Vec3d posDiff = posTarget.subtract(posSource);
                Vec3d vecMotion = new Vec3d(target.motionX, target.motionY, target.motionZ);
                if (posDiff.add(vecMotion).lengthVector() > posDiff.lengthVector()) {
                    target.motionX = -target.motionX;
                    target.motionZ = -target.motionZ;
                }
            }
        }
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!SkillHelper.isActive(target, ModEffects.BURNING) && target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 5.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
        ModEffects.BURNING.set(target, skillData);
    }

    //* Entity *//

    public int getLevel(SkillInfo.IInfoUpgradeable info) {
        return info.getLevel();
    }

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

    public int getDoTTime(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.dotTime, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalNeutralEffect);
        return (int) (result * getEffectiveness());
    }

    public double getRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().range, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalRange);
        return (result * getEffectiveness());
    }

    public double getRingRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.range, level, levelMax);
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
                        description.add(TextHelper.translate("desc.stats.ring_range", TextHelper.format2FloatPoint(getRingRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getDoTTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.ring_range", TextHelper.format2FloatPoint(getRingRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                            description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                            description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getDoTTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            if (c.level >= tokens && c.getExperienceTotal(entity) >= xp) {
                Capabilities.get(entity).filter(a -> !a.isOwned(this)).ifPresent(a -> {
                    Skill[] skillUnlockOrder = Arrays.copyOf(c.skillUnlockOrder, c.skillUnlockOrder.length + 1);
                    skillUnlockOrder[skillUnlockOrder.length - 1] = this;
                    c.skillUnlockOrder = skillUnlockOrder;
                });
                c.consumeExperienceFromTotal(entity, xp);
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
        Configuration.getSyncValues().range = Configuration.getValues().range;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().extra.range = Configuration.getValues().extra.range;
        Configuration.getSyncValues().extra.dotTime = Configuration.getValues().extra.dotTime;
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
        NBTHelper.setArray(compound, "extra.range", Configuration.getValues().extra.range);
        NBTHelper.setArray(compound, "extra.dotTime", Configuration.getValues().extra.dotTime);
        NBTHelper.setArray(compound, "extra.dot", Configuration.getValues().extra.dot);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().range = NBTHelper.getArray(compound, "range");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.range = NBTHelper.getArray(compound, "extra.range");
        Configuration.getSyncValues().extra.dotTime = NBTHelper.getArray(compound, "extra.dotTime");
        Configuration.getSyncValues().extra.dot = NBTHelper.getArray(compound, "extra.dot");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.RING_OF_FIRE)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.RING_OF_FIRE)
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
                    "(0+){32 * 20 + 28 * 20 * (1 - ((1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))))}",
                    "(25+){22 * 20 + 10 * 20 * (1- (((e^(0.1 * ((x-24) / (y-24))) - 1)/((e^0.1) - 1))))}",
                    "(50){(18 * 20)}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){6 * 20}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){15}",
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] range = {
                        "(0+){4}",
                };
                @Config.Comment("Damage Over Time specific duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] dotTime = {
                        "(0+){6 * 20}"
                };
                @Config.Comment("Damage Over Time Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] dot = {
                        "(0+){4}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){300}",
                        "(1+){4 * x}",
                        "(50){4 * x + 4 * x * 0.1}"
                };
            }
        }
    }
}
