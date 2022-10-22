package arekkuusu.enderskills.common.skill.ability.offence.light;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableExplode;
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

public class RadiantRay extends BaseAbility implements IScanEntities, IImpact, IExpand, IFindEntity, ISkillAdvancement {

    public RadiantRay() {
        super(LibNames.RADIANT_RAY, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double distance = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getRange(abilityInfo));;
            double range = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getLightRange(abilityInfo));;
            double time = arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));;
            double damage = getDamage(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "damage", damage);
            NBTHelper.setDouble(compound, "distance", distance);
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setDouble(compound, "time", time);
            SkillData data = SkillData.of(this)
                    .put(compound)
                    .create();
            EntityThrowableData.throwFor(owner, distance, data, false);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.RADIANT_RAY_CAST, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        int time = skillData.nbt.getInteger("time");
        double radius = skillData.nbt.getDouble("range");
        Vec3d hitVector = trace.hitVec;
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
            hitVector = new Vec3d(hitVector.x, hitVector.y + trace.entityHit.getEyeHeight(), hitVector.z);
        }
        SkillData status = SkillData.of(this)
                .by(skillData.id + ":" + skillData.skill.getRegistryName())
                .with(1)
                .put(skillData.nbt.copy(), skillData.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(source.world, owner, status, time + 5);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        spawn.growTicks = 5;
        source.world.spawnEntity(spawn); //MANIFEST B L O O D!!

        if (owner.world instanceof WorldServer) {
            ((WorldServer) owner.world).playSound(null, hitVector.x, hitVector.y, hitVector.z, ModSounds.RADIANT_RAY_RELEASE, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (SkillHelper.isActive(target, ModEffects.GLOWING)) {
            ModEffects.GLOWING.activate(target, skillData);
        } else {
            ModEffects.GLOWING.set(target, skillData);
        }

        double damage = skillData.nbt.getDouble("damage");
        SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        damageSource.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);

        if(event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            target.attackEntityFrom(event.getSource(), event.toFloat());
        }

        if (target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.OFFLIGHT_ONHIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
    //* Entity *//

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getDamage(AbilityInfo info) {
        return this.config.get(this, "DAMAGE", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "DURATION", info.getLevel());
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public double getLightRange(AbilityInfo info) {
        return this.config.get(this, "SIZE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.radiant_range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getLightRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.damage", TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
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
                                description.add(TextHelper.translate("desc.stats.radiant_range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getLightRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.damage", TextHelper.format2FloatPoint(getDamage(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.RADIANT_RAY;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_OFFENCE_CONFIG + LibNames.RADIANT_RAY;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Values();
        public static final Values CONFIG = new Values();

        public static class Values {

            public String[] dsl = {
                    "",
                    "│ ",
                    "│ min_level: 0",
                    "│ max_level: 50",
                    "│ ",
                    "",
                    "┌ COOLDOWN (",
                    "│     shape: flat",
                    "│     start: 18s",
                    "│     end:   6s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 10s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 7s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ RANGE (",
                    "│     shape: flat",
                    "│     start: 12b",
                    "│     end:   18b",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 15b",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 25b",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ SIZE (",
                    "│     shape: flat",
                    "│     start: 1b",
                    "│     end:   3b",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 1.5b",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 2b",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DURATION (",
                    "│     shape: none",
                    "│     start: 0.25s",
                    "└ )",
                    "",
                    "┌ DAMAGE (",
                    "│     shape: flat",
                    "│     start: 0.5h",
                    "│     end:   2.5h",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 1h",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 2h",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     start: 170",
                    "│     end:   infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         value: {start}",
                    "│     ]",
                    "│ ",
                    "│     {1 to 49} [",
                    "│         shape: multiply 4",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: f(x, y) -> 4 * x + 4 * x * 0.1",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
