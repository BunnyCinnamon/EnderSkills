package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IImpact;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class Contaminate extends BaseAbility implements IImpact, ISkillAdvancement {

    public Contaminate() {
        super(LibNames.CONTAMINATE, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
        double distance = getRange(abilityInfo);

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            int time = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "dot", getDoT(abilityInfo));
            NBTHelper.setInteger(compound, "time", getTime(abilityInfo));
            NBTHelper.setDouble(compound, "damage", getDamage(abilityInfo));
            SkillData data = SkillData.of(this)
                    .with(time)
                    .put(compound)
                    .create();
            EntityThrowableData.throwForTarget(owner, distance, data, false);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.CONTAMINATE, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (RayTraceHelper.isEntityTrace(trace, TeamHelper.SELECTOR_ENEMY.apply(owner))) {
            apply((EntityLivingBase) trace.entityHit, skillData);
            sync((EntityLivingBase) trace.entityHit, skillData);
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("damage");
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("dot");
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        source.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            entity.attackEntityFrom(event.getSource(), event.toFloat() / data.time);
        }
        EnderSkills.getProxy().addToQueue(() -> ModEffects.SLOWED.set(entity, data, 0.6D));
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getDoT(AbilityInfo info) {
        return this.config.get(this, "DOT", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public double getDamage(AbilityInfo info) {
        return this.config.get(this, "DAMAGE", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "DURATION", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.initial_dot", TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
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
                                description.add(TextHelper.translate("desc.stats.initial_dot", TextHelper.format2FloatPoint(getDamage(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                                description.add(TextHelper.translate("desc.stats.dot", TextHelper.format2FloatPoint(getDoT(infoNew) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.BLEED;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLOOD_OFFENCE_CONFIG + LibNames.CONTAMINATE;
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
                    "⠀    start: 35s",
                    "⠀    end:   16s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 23s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 18s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀DURATION (",
                    "⠀    curve: none",
                    "⠀    value: 7s",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀RANGE (",
                    "⠀    curve: flat",
                    "⠀    start: 6b",
                    "⠀    end:   14b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 9b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 11b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀DAMAGE (",
                    "⠀    curve: flat",
                    "⠀    start: 4h",
                    "⠀    end:   12h",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 6h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 10h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀DOT (",
                    "⠀    curve: flat",
                    "⠀    start: 4h",
                    "⠀    end:   16h",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 6h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 14h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
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
