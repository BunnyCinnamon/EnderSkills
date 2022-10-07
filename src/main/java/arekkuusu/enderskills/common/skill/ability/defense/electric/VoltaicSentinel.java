package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.EntityVoltaicSentinel;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Optional;

public class VoltaicSentinel extends BaseAbility {

    public VoltaicSentinel() {
        super(LibNames.VOLTAIC_SENTINEL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((SkillInfo.IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (!((SkillInfo.IInfoCooldown) skillInfo).hasCooldown() && isActionable(owner) && canActivate(owner)) {
                if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                    abilityInfo.setCooldown(getCooldown(abilityInfo));
                }
                EntityVoltaicSentinel sentinel = new EntityVoltaicSentinel(owner.world);
                sentinel.setPosition(owner.posX, owner.posY + owner.height + 0.1D, owner.posZ);
                sentinel.setOwnerId(owner.getUniqueID());
                sentinel.setFollowId(owner.getUniqueID());
                sentinel.setMaxHealth(getHealth(abilityInfo));
                sentinel.setHealth(getHealth(abilityInfo));
                sentinel.setDamage(getDamage(abilityInfo));
                owner.world.spawnEntity(sentinel);

                int time = getTime(abilityInfo);
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setEntity(compound, sentinel, "sentinel");
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(time)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                apply(owner, data);
                sync(owner, data);
                sync(owner);

                if (owner.world instanceof WorldServer) {
                    ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.VOLTAIC_SENTINEL_SUMMON, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
                }
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityVoltaicSentinel.class, data.nbt, "sentinel")).ifPresent(e -> {
                    Entity found = RayTraceHelper.getEntityLookedAt(owner, 5).orElse(null);
                    if (!(found instanceof EntityLivingBase) || !TeamHelper.SELECTOR_ALLY.apply(owner).test(found))
                        found = owner;
                    e.setFollowId(found.getUniqueID());
                    e.teleportTo((EntityLivingBase) found);
                });
            });
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (NBTHelper.getEntity(EntityVoltaicSentinel.class, data.nbt, "sentinel") == null) {
            unapply(owner, data);
            async(owner, data);
        }
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public float getDamage(AbilityInfo info) {
        return (float) this.config.get(this, "DAMAGE", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalNegativeEffect);
    }

    public float getHealth(AbilityInfo info) {
        return (float) this.config.get(this, "HEALTH", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
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
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
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
                                description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.health", TextHelper.format2FloatPoint(getHealth(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                                description.add(TextHelper.translate("desc.stats.damage", TextHelper.format2FloatPoint(getDamage(abilityInfo) / 2D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.SHOCKING_AURA;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.VOLTAIC_SENTINEL;
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
                    "⠀    start: 120s",
                    "⠀    end:   80s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 90s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 85s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀DURATION (",
                    "⠀    curve: flat",
                    "⠀    start: 30s",
                    "⠀    end:   120s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 90s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 100s",
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
                    "⠀    start: 5b",
                    "⠀    end:   16b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 12b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 14b",
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
                    "⠀    start: 1h",
                    "⠀    end:   5h",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 2h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 3h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀HEALTH (",
                    "⠀    curve: flat",
                    "⠀    start: 10h",
                    "⠀    end:   40h",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 20h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 35h",
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
                    "⠀    start: 900",
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
