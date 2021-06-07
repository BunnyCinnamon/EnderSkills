package arekkuusu.enderskills.common.skill.ability.offence.wind;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class Push extends BaseAbility implements IImpact, IScanEntities, IExpand, IFindEntity, ISkillAdvancement {

    public Push() {
        super(LibNames.PUSH, new AbilityProperties());
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
            double distance = getRange(abilityInfo);
            double range = getPushRange(abilityInfo);
            double force = getPush(abilityInfo);
            int time = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setDouble(compound, "force", force);
            NBTHelper.setInteger(compound, "time", time);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(10)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            EntityThrowableData.throwForTarget(owner, distance, data, false);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.PUSH, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        double radius = skillData.nbt.getDouble("range");
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, skillData, EntityPlaceableData.MIN_TIME);
        spawn.rotationPitch = -source.rotationPitch;
        spawn.rotationYaw = source.rotationYaw;
        Vec3d hitVector = trace.hitVec;
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ENEMY.apply(owner))) {
            hitVector = new Vec3d(hitVector.x, hitVector.y + trace.entityHit.getEyeHeight(), hitVector.z);
            apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);
        }
        Vec3d lookVector = source.getLook(1F);
        Vec3d targetVector = hitVector.subtract(
                lookVector.x,
                lookVector.y,
                lookVector.z
        );
        spawn.setPosition(targetVector.x, targetVector.y, targetVector.z);
        spawn.setRadius(radius);
        source.world.spawnEntity(spawn); //MANIFEST B L O O D!!

        if (source.world instanceof WorldServer) {
            ((WorldServer) source.world).playSound(null, source.posX, source.posY, source.posZ, ModSounds.WIND_ON_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (source.world.rand.nextFloat() - source.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        return RayTraceHelper.getEntitiesInCone(source, size, 60, TeamHelper.SELECTOR_ENEMY.apply(owner));
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        Vec3d lookVector = source.getLook(1F);
        NBTHelper.setVector(skillData.nbt, "vector", lookVector);
        apply(target, skillData);
        sync(target, skillData);

        if (target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.WIND_ON_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        Vec3d vector = NBTHelper.getVector(data.nbt, "vector");
        double distance = NBTHelper.getDouble(data.nbt, "force");
        Vec3d from = target.getPositionVector();
        Vec3d to = from.addVector(
                vector.x * distance,
                vector.y * distance,
                vector.z * distance
        );
        MotionHelper.moveEntity(to, from, target);
        if (target.collidedHorizontally) {
            target.motionY = 0;
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EnderSkills.getProxy().addToQueue(() -> ModEffects.STUNNED.set(entity, data, data.nbt.getInteger("time")));
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getPushRange(AbilityInfo info) {
        return this.config.get(this, "SIZE", info.getLevel());
    }

    public double getPush(AbilityInfo info) {
        return this.config.get(this, "FORCE", info.getLevel());
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "STUN", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.push_distance", TextHelper.format2FloatPoint(getPush(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        if (abilityInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                                AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                                infoNew.setLevel(infoNew.getLevel() + 1);
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                                description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.push_distance", TextHelper.format2FloatPoint(getPush(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.SLASH;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_OFFENCE_CONFIG + LibNames.PUSH;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static final Configuration.Values CONFIG = new Configuration.Values();

        public static class Values {

            public String[] dsl = {
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀",
                    "⠀min_level: 0",
                    "⠀max_level: 50",
                    "⠀",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀COOLDOWN (",
                    "⠀    curve: flat",
                    "⠀    start: 24s",
                    "⠀    end:   8s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 14s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 10s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀RANGE (",
                    "⠀    curve: flat",
                    "⠀    start: 18b",
                    "⠀    end:   32b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 24b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 28b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀FORCE (",
                    "⠀    curve: flat",
                    "⠀    start: 8b",
                    "⠀    end:   24b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 18b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 20b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀SIZE (",
                    "⠀    curve: none",
                    "⠀    value: 2b",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀STUN (",
                    "⠀    curve: none",
                    "⠀    value: 2s",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀XP (",
                    "⠀    curve: flat",
                    "⠀    start: 300",
                    "⠀    end:   infinite",
                    "⠀",
                    "⠀    {0} [",
                    "⠀        curve: none",
                    "⠀        value: {start}",
                    "⠀    ]",
                    "⠀",
                    "⠀    {1 to 49} [",
                    "⠀        curve: multiply 4",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: f(x, y) -> 4 * x + 4 * x * 0.1",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
            };
        }
    }
    /*Config Section*/
}
