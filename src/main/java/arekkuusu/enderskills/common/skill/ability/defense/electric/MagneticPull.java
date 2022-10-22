package arekkuusu.enderskills.common.skill.ability.defense.electric;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.sounds.MagneticPullSound;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class MagneticPull extends BaseAbility {

    public MagneticPull() {
        super(LibNames.MAGNETIC_PULL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((SkillInfo.IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }

            int time = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));;
            double range = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getRange(abilityInfo));;
            double stun = getStun(abilityInfo);
            double slow = getSlow(abilityInfo);
            double pull = getPull(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "time", time);
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setDouble(compound, "stun", stun);
            NBTHelper.setDouble(compound, "slow", slow);
            NBTHelper.setDouble(compound, "pull", pull);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(time)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            apply(owner, data);
            sync(owner, data);
            sync(owner);
        }
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MagneticPullSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        double distance = NBTHelper.getDouble(data.nbt, "range") * MathHelper.clamp(((double) tick / (10)), 0D, 1D);
        double slow = NBTHelper.getDouble(data.nbt, "slow");
        double pull = NBTHelper.getDouble(data.nbt, "pull");
        int stun = NBTHelper.getInteger(data.nbt, "stun");
        Vec3d pos = owner.getPositionVector();
        pos = new Vec3d(pos.x, pos.y + owner.height / 2, pos.z);
        Vec3d min = pos.subtract(0.5D, 0.5D, 0.5D);
        Vec3d max = pos.addVector(0.5D, 0.5D, 0.5D);
        AxisAlignedBB bb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
        owner.world.getEntitiesWithinAABB(EntityLivingBase.class, bb.grow(distance), TeamHelper.SELECTOR_ENEMY.apply(owner)).forEach(target -> {
            if (!isClientWorld(target)) {
                if (SkillHelper.isActive(target, ModEffects.ELECTRIFIED)) {
                    ModEffects.ELECTRIFIED.propagate(target, data, stun);
                }
                if (target.isWet() && tick % 20 == 0) {
                    target.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 2);
                }
                ModEffects.SLOWED.set(target, data, slow);
            }
            if (!isClientWorld(target) || target instanceof EntityPlayer) {
                MotionHelper.pull(owner.getPositionVector(), target, pull);
                if (target.collidedHorizontally) {
                    target.motionY = 0;
                }
            }
        });
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getSlow(AbilityInfo info) {
        return this.config.get(this, "SLOW", info.getLevel());
    }

    public float getStun(AbilityInfo info) {
        return (float) this.config.get(this, "STUN", info.getLevel());
    }

    public double getPull(AbilityInfo info) {
        return this.config.get(this, "PULL", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.slow", TextHelper.format2FloatPoint(getSlow(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        description.add(TextHelper.translate("desc.stats.when_electrified"));
                        description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getStun(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
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
                                description.add(TextHelper.translate("desc.stats.slow", TextHelper.format2FloatPoint(getSlow(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                                description.add(TextHelper.translate("desc.stats.when_electrified"));
                                description.add(TextHelper.translate("desc.stats.stun", TextHelper.format2FloatPoint(getStun(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
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
    public static final String CONFIG_FILE = LibNames.ELECTRIC_DEFENSE_CONFIG + LibNames.MAGNETIC_PULL;
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
                    "│     min: 30s",
                    "│     max: 12s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   20s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   16s",
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
                    "│     min: 4s",
                    "│     max: 8s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   6s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   7s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ RANGE (",
                    "│     shape: flat",
                    "│     min: 3b",
                    "│     max: 9b",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   4b",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   6b",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ PULL (",
                    "│     shape: flat",
                    "│     min: 1b",
                    "│     max: 2b",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: {min}",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ SLOW (",
                    "│     shape: none",
                    "│     value: 50%",
                    "└ )",
                    "",
                    "┌ STUN (",
                    "│     shape: none",
                    "│     value: 3s",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 300",
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
