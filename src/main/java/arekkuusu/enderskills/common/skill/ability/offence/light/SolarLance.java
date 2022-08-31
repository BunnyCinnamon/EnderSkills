package arekkuusu.enderskills.common.skill.ability.offence.light;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.sounds.FireSpiritSound;
import arekkuusu.enderskills.client.sounds.FireSpiritSound2;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SolarLance extends BaseAbility implements ISkillAdvancement {

    public SolarLance() {
        super(LibNames.SOLAR_LANCE, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        Capabilities.get(owner).ifPresent(capability -> {
            if (!SkillHelper.isActiveFrom(owner, this)) {
                if (!((IInfoCooldown) skillInfo).hasCooldown() && canActivate(owner)) {
                    if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                        abilityInfo.setCooldown(getCooldown(abilityInfo));
                    }
                    double distance = getRange(abilityInfo);
                    double range = getLanceRange(abilityInfo);
                    double time = getPenis(abilityInfo);
                    double damage = getDamage(abilityInfo);
                    NBTTagCompound compound = new NBTTagCompound();
                    NBTHelper.setEntity(compound, owner, "owner");
                    NBTHelper.setDouble(compound, "damage", damage);
                    NBTHelper.setDouble(compound, "distance", distance);
                    NBTHelper.setDouble(compound, "range", range);
                    NBTHelper.setDouble(compound, "time", time);
                    SkillData data = SkillData.of(this)
                            .by(owner)
                            .with(INDEFINITE)
                            .put(compound)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();
                    apply(owner, data);
                    sync(owner, data);
                    sync(owner);
                }
            } else {
                SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                    unapply(owner, data);
                    async(owner, data);
                });
            }
        });
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            makeSound(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new FireSpiritSound(entity));
        Minecraft.getMinecraft().getSoundHandler().playSound(new FireSpiritSound2(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (tick % 20 == 0 && (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode)) {
            Capabilities.endurance(owner).ifPresent(capability -> {
                int drain = ModAttributes.ENDURANCE.getEnduranceDrain(this);
                if (capability.getEndurance() - drain >= 0) {
                    capability.setEndurance(capability.getEndurance() - drain);
                    capability.setEnduranceDelay(30);
                    if (owner instanceof EntityPlayerMP) {
                        PacketHelper.sendEnduranceSync((EntityPlayerMP) owner);
                    }
                } else {
                    unapply(owner, data);
                    async(owner, data);
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getAmount() <= 0) return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
        if (TeamHelper.SELECTOR_ENEMY.apply(attacker).test(target)) {
            SkillHelper.getActiveFrom(attacker, this).ifPresent(data -> {
                if (!SkillHelper.isActive(target, ModEffects.BURNING)) {
                    if (target.world instanceof WorldServer) {
                        ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                }
                ModEffects.BURNING.set(target, data);
            });
        }
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getDamage(AbilityInfo info) {
        return this.config.get(this, "DAMAGE", info.getLevel());
    }

    public int getPenis(AbilityInfo info) {
        return (int) this.config.get(this, "PIERCING", info.getLevel());
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public double getLanceRange(AbilityInfo info) {
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
                        description.add(TextHelper.translate("desc.stats.lance_range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getLanceRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
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
                                description.add(TextHelper.translate("desc.stats.lance_range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getLanceRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
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
        return ModAbilities.FIRE_SPIRIT;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_OFFENCE_CONFIG + LibNames.SOLAR_LANCE;
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
        public static final Configuration.Values CONFIG_SYNC = new Values();
        public static final Values CONFIG = new Values();

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
                    "⠀    start: 16s",
                    "⠀    end:   2s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 8s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 6s",
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
                    "⠀    start: 10b",
                    "⠀    end:   30b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 15b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 25b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀SIZE (",
                    "⠀    curve: flat",
                    "⠀    start: 1b",
                    "⠀    end:   3b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 1.5b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 2b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀PIERCING (",
                    "⠀    curve: flat",
                    "⠀    start: 1",
                    "⠀    end:   6",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 3",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 5",
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
                    "⠀    end:   6h",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 3h",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 5h",
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
                    "⠀    start: 600",
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
