package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.entity.EntityPortal;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class Portal extends BaseAbility implements ISkillAdvancement {

    public Portal() {
        super(LibNames.PORTAL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (isActionable(owner) && !((IInfoCooldown) skillInfo).hasCooldown()) {
                EntityPortal portal = new EntityPortal(owner.world, getTime(abilityInfo));
                portal.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
                owner.world.spawnEntity(portal);
                int time = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));;
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, portal, "portal");
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setInteger(compound, "time", time);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.SAME)
                        .create();
                apply(owner, data);
                sync(owner, data);
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                EntityPortal originalPortal = NBTHelper.getEntity(EntityPortal.class, data.nbt, "portal");
                if (originalPortal != null) {
                    if (originalPortal.getTarget() == null) {
                        if (isActionable(owner) && canActivate(owner)) {
                            EntityPortal portal = new EntityPortal(owner.world, NBTHelper.getInteger(data.nbt, "time"));
                            portal.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
                            owner.world.spawnEntity(portal);
                            originalPortal.setTarget(portal);
                            portal.setTarget(originalPortal);
                            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                                abilityInfo.setCooldown(getCooldown(abilityInfo));
                            }
                            sync(owner);
                        }
                    } else {
                        originalPortal.setDead();
                        originalPortal.getTarget().setDead();
                    }
                }
            });
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        EntityPortal portal = NBTHelper.getEntity(EntityPortal.class, data.nbt, "portal");
        if (portal == null) {
            Capabilities.get(owner).flatMap(c -> c.getOwned(this)).ifPresent(skillInfo -> {
                if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                    ((AbilityInfo) skillInfo).setCooldown(getCooldown((AbilityInfo) skillInfo));
                }
                sync(owner);
            });
            unapply(owner, data);
            async(owner, data);
        }
    }

    public int getMaxLevel() {
        return this.config.max_level;
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
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.WARP;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.PORTAL;
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
                    "",
                    "┌ v1.0",
                    "│ ",
                    "├ min_level: 0",
                    "├ max_level: 50",
                    "└ ",
                    "",
                    "┌ COOLDOWN (",
                    "│     shape: flat",
                    "│     min: 60s",
                    "│     max: 34s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   48s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   40s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DURATION (",
                    "│     shape: flat",
                    "│     min: 8s",
                    "│     max: 16s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   12s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   14s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 600",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: {min}",
                    "│     ]",
                    "│ ",
                    "│     {1 to 49} [",
                    "│         shape: multiply 4",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: solve for 4 * {level} + 4 * {level} * 0.1",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
