package arekkuusu.enderskills.common.handler;

import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Pair;
import arekkuusu.enderskills.client.gui.GuiPauseAll;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementTab;
import arekkuusu.enderskills.client.gui.data.*;
import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancement;
import arekkuusu.enderskills.client.gui.widgets.SkillAdvancementTabType;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.lib.LibGui;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.BasicSkillAdvancement;
import arekkuusu.enderskills.common.skill.ability.defense.fire.HomeStar;
import arekkuusu.enderskills.common.skill.ability.defense.fire.RingOfFire;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.ExtraJump;
import arekkuusu.enderskills.common.skill.ability.offence.blood.BloodPool;
import arekkuusu.enderskills.common.skill.ability.offence.light.BarrageWisp;
import arekkuusu.enderskills.common.skill.ability.offence.light.SolarLance;
import arekkuusu.enderskills.common.skill.attribute.AttributeSkillAdvancement;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.function.BiFunction;

public final class GuiHandler implements IGuiHandler {

    public static final Map<String, BiFunction<Skill, Integer, Number>> VALUES = Maps.newHashMap();
    public static final Map<Pair<ResourceLocation, String>, BiFunction<Skill, Integer, Number>> SPECIFIC_VALUES = Maps.newHashMap();
    public static final Map<String, String> SUFFIX = Maps.newHashMap();
    public static final Map<Pair<ResourceLocation, String>, String> SPECIFIC_SUFFIX = Maps.newHashMap();
    public static final Map<ResourceLocation, SkillAdvancement> ADVANCEMENTS = Maps.newHashMap();

    public static void setValue(String name, BiFunction<Skill, Integer, Number> function) {
        VALUES.put(name, function);
    }

    public static void setValue(String id, String name, BiFunction<Skill, Integer, Number> function) {
        SPECIFIC_VALUES.put(new Pair<>(new ResourceLocation(LibMod.MOD_ID, id), name), function);
    }

    public static void setSuffix(String name, String suffix) {
        SUFFIX.put(name, suffix);
    }

    public static void setSuffix(String id, String name, String suffix) {
        SPECIFIC_SUFFIX.put(new Pair<>(new ResourceLocation(LibMod.MOD_ID, id), name), suffix);
    }

    public static void setAdvancement(String id, Skill original, Skill parent) {
        ADVANCEMENTS.put(new ResourceLocation(LibMod.MOD_ID, id), new BasicSkillAdvancement(original, parent));
    }

    public static void setAdvancement(String id, Skill original) {
        ADVANCEMENTS.put(new ResourceLocation(LibMod.MOD_ID, id), new AttributeSkillAdvancement(original));
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case LibGui.LEVEL_EDITING:
                TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
                if (tile instanceof TileAltar) {
                    TileAltar altar = (TileAltar) tile;
                    SkillAdvancementConditionAltar.ALTAR_JUICE = altar.getLevel();
                    SkillAdvancementConditionAltar.IS_ULTIMATE = altar.isUltimate();
                } else {
                    SkillAdvancementConditionAltar.ALTAR_JUICE = 1;
                    SkillAdvancementConditionAltar.IS_ULTIMATE = true;
                }
                //
                GuiHandler.setValue("MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level) * 100);
                GuiHandler.setValue(LibNames.HEART_BOOST, "MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level));
                GuiHandler.setValue(LibNames.ENDURANCE, "MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level));
                GuiHandler.setValue(LibNames.JUMP_HEIGHT, "MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level));
                GuiHandler.setValue(LibNames.KNOCKBACK, "MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level));
                GuiHandler.setValue(LibNames.GLOWING, "MODIFIER", (skill, level) -> DSLDefaults.getModifier(skill, level) / 2);
                GuiHandler.setValue("COOLDOWN", (skill, level) -> DSLDefaults.getCooldown(skill, level) / 20);
                GuiHandler.setValue("DURATION", (skill, level) -> DSLDefaults.triggerDuration(player, skill, level).getAmount() / 20);
                GuiHandler.setValue("TRUE_DAMAGE", (skill, level) -> DSLDefaults.getTrueDamage(skill, level) * 100);
                GuiHandler.setValue("DAMAGE", (skill, level) -> DSLDefaults.getDamage(skill, level) / 2);
                GuiHandler.setValue("DAMAGE_MIRROR", (skill, level) -> DSLDefaults.getDamageMimicry(skill, level) * 100);
                GuiHandler.setValue("HEALTH", (skill, level) -> DSLDefaults.getHealth(skill, level) / 2);
                GuiHandler.setValue("STUN", (skill, level) -> DSLDefaults.getStun(skill, level) / 20);
                GuiHandler.setValue("RANGE", (skill, level) -> DSLDefaults.triggerRange(player, skill, level).getAmount());
                GuiHandler.setValue("RANGE_EXTRA", (skill, level) -> DSLDefaults.triggerRangeExtension(player, skill, level).getAmount());
                GuiHandler.setValue("HEIGHT", (skill, level) -> DSLDefaults.triggerHeight(player, skill, level).getAmount());
                GuiHandler.setValue("WIDTH", (skill, level) -> DSLDefaults.triggerWidth(player, skill, level).getAmount());
                GuiHandler.setValue("SIZE", (skill, level) -> DSLDefaults.triggerSize(player, skill, level).getAmount());
                GuiHandler.setValue("FORCE", (skill, level) -> DSLDefaults.getForce(skill, level));
                GuiHandler.setValue("SLOW", (skill, level) -> DSLDefaults.getSlow(skill, level) * 100);
                GuiHandler.setValue("POWER", (skill, level) -> DSLDefaults.getPower(skill, level));
                GuiHandler.setValue("DOT", (skill, level) -> DSLDefaults.getDamageOverTime(skill, level) / 2);
                GuiHandler.setValue("TRUE_DOT", (skill, level) -> DSLDefaults.getTrueDamageOverTime(skill, level) * 100);
                GuiHandler.setValue("DOT_DURATION", (skill, level) -> DSLDefaults.triggerDamageDuration(player, skill, level).getAmount() / 20);
                GuiHandler.setValue("INTERVAL", (skill, level) -> DSLDefaults.triggerIntervalDuration(player, skill, level).getAmount() / 20);
                GuiHandler.setValue("PULSE_RANGE", (skill, level) -> HomeStar.getPulseRange(player, level).getAmount());
                GuiHandler.setValue("PULSE_INTERVAL", (skill, level) -> HomeStar.getPulseInterval(level) / 20);
                GuiHandler.setValue("PULSE_DAMAGE", (skill, level) -> HomeStar.getPulseDamage(level) / 2);
                GuiHandler.setValue("PULSE_DOT", (skill, level) -> HomeStar.getPulseDamageOverTime(level) / 2);
                GuiHandler.setValue("PULSE_DOT_DURATION", (skill, level) -> HomeStar.getPulseDamageOverTimeDuration(player, level).getAmount() / 20);
                GuiHandler.setValue("DELAY", (skill, level) -> DSLDefaults.getDelay(skill, level) / 20);
                GuiHandler.setValue("REGEN", (skill, level) -> DSLDefaults.getRegen(skill, level) / 20);
                GuiHandler.setValue("RING_DURATION", (skill, level) -> RingOfFire.getRingDuration(player, level).getAmount() / 20);
                GuiHandler.setValue("RING_RANGE", (skill, level) -> RingOfFire.getRingRange(player, level).getAmount());
                GuiHandler.setValue("HEAL", (skill, level) -> DSLDefaults.getHeal(skill, level));
                GuiHandler.setValue("HEAL_DURATION", (skill, level) -> DSLDefaults.triggerHealDuration(player, skill, level).getAmount());
                GuiHandler.setValue("DISPLACEMENT", (skill, level) -> DSLDefaults.getDisplacement(skill, level));
                GuiHandler.setValue("JUMPS", (skill, level) -> ExtraJump.getJumps(level));
                GuiHandler.setValue("REDUCTION", (skill, level) -> DSLDefaults.getReduction(skill, level));
                GuiHandler.setValue("SPEED", (skill, level) -> DSLDefaults.getSpeed(skill, level));
                GuiHandler.setValue("POOL_RANGE", (skill, level) -> BloodPool.getPoolRange(player, level).getAmount());
                GuiHandler.setValue("POOL_DURATION", (skill, level) -> BloodPool.getPoolDuration(player, level).getAmount() / 20);
                GuiHandler.setValue("AMOUNT", (skill, level) -> BarrageWisp.getAmount(level));
                GuiHandler.setValue("DELAY", (skill, level) -> DSLDefaults.getDelay(skill, level) / 20);
                GuiHandler.setValue("PIERCING", (skill, level) -> SolarLance.getPiercing(level));
                //
                GuiHandler.setSuffix("MODIFIER", "suffix_percentage");
                GuiHandler.setSuffix(LibNames.HEART_BOOST, "MODIFIER", "suffix_hearts");
                GuiHandler.setSuffix(LibNames.ENDURANCE, "MODIFIER", "suffix_percentage");
                GuiHandler.setSuffix(LibNames.JUMP_HEIGHT, "MODIFIER", "suffix_blocks");
                GuiHandler.setSuffix(LibNames.KNOCKBACK, "MODIFIER", "suffix_force");
                GuiHandler.setSuffix(LibNames.GLOWING, "MODIFIER", "suffix_hearts");
                GuiHandler.setSuffix(LibNames.SOLAR_LANCE, "AMOUNT", "suffix_enemies");
                GuiHandler.setSuffix("COOLDOWN", "suffix_time");
                GuiHandler.setSuffix("DURATION", "suffix_time");
                GuiHandler.setSuffix("DAMAGE", "suffix_hearts");
                GuiHandler.setSuffix("TRUE_DAMAGE", "suffix_percentage_hearts");
                GuiHandler.setSuffix("DAMAGE_MIRROR", "suffix_percentage");
                GuiHandler.setSuffix("HEALTH", "suffix_hearts");
                GuiHandler.setSuffix("STUN", "suffix_time");
                GuiHandler.setSuffix("RANGE", "suffix_blocks");
                GuiHandler.setSuffix("RANGE_EXTRA", "suffix_blocks");
                GuiHandler.setSuffix("HEIGHT", "suffix_blocks");
                GuiHandler.setSuffix("WIDTH", "suffix_blocks");
                GuiHandler.setSuffix("SIZE", "suffix_blocks");
                GuiHandler.setSuffix("FORCE", "suffix_force");
                GuiHandler.setSuffix("SLOW", "suffix_percentage");
                GuiHandler.setSuffix("POWER", "suffix_percentage");
                GuiHandler.setSuffix("DOT", "suffix_hearts");
                GuiHandler.setSuffix("TRUE_DOT", "suffix_percentage_hearts");
                GuiHandler.setSuffix("DOT_DURATION", "suffix_time");
                GuiHandler.setSuffix("INTERVAL", "suffix_time");
                GuiHandler.setSuffix("PULSE_RANGE", "suffix_blocks");
                GuiHandler.setSuffix("PULSE_INTERVAL", "suffix_time");
                GuiHandler.setSuffix("PULSE_DAMAGE", "suffix_hearts");
                GuiHandler.setSuffix("PULSE_DOT", "suffix_hearts");
                GuiHandler.setSuffix("PULSE_DOT_DURATION", "suffix_time");
                GuiHandler.setSuffix("RING_DURATION", "suffix_time");
                GuiHandler.setSuffix("RING_RANGE", "suffix_blocks");
                GuiHandler.setSuffix("HEAL", "suffix_hearts");
                GuiHandler.setSuffix("HEAL_DURATION", "suffix_time");
                GuiHandler.setSuffix("DISPLACEMENT", "suffix_blocks");
                GuiHandler.setSuffix("JUMPS", "suffix_blocks");
                GuiHandler.setSuffix("REDUCTION", "suffix_percentage");
                GuiHandler.setSuffix("SPEED", "suffix_percentage");
                GuiHandler.setSuffix("POOL_RANGE", "suffix_blocks");
                GuiHandler.setSuffix("POOL_DURATION", "suffix_time");
                GuiHandler.setSuffix("AMOUNT", "suffix_amount");
                GuiHandler.setSuffix("DELAY", "suffix_time");
                GuiHandler.setSuffix("PIERCING", "suffix_amount");
                GuiHandler.setSuffix("AMOUNT", "suffix_amount");
                //
                GuiHandler.setAdvancement(LibNames.DAMAGE_RESISTANCE, ModAttributes.DAMAGE_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.EXPLOSION_RESISTANCE, ModAttributes.EXPLOSION_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.FIRE_RESISTANCE, ModAttributes.FIRE_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.HEART_BOOST, ModAttributes.HEART_BOOST);
                GuiHandler.setAdvancement(LibNames.KNOCKBACK_RESISTANCE, ModAttributes.KNOCKBACK_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.MAGIC_RESISTANCE, ModAttributes.MAGIC_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.ENDURANCE, ModAttributes.ENDURANCE);
                GuiHandler.setAdvancement(LibNames.FALL_RESISTANCE, ModAttributes.FALL_RESISTANCE);
                GuiHandler.setAdvancement(LibNames.JUMP_HEIGHT, ModAttributes.JUMP_HEIGHT);
                GuiHandler.setAdvancement(LibNames.SPEED, ModAttributes.SPEED);
                GuiHandler.setAdvancement(LibNames.STEALTH_DAMAGE, ModAttributes.STEALTH_DAMAGE);
                GuiHandler.setAdvancement(LibNames.SWIM_SPEED, ModAttributes.SWIM_SPEED);
                GuiHandler.setAdvancement(LibNames.ABILITY_DURATION, ModAttributes.ABILITY_DURATION);
                GuiHandler.setAdvancement(LibNames.ABILITY_POWER, ModAttributes.ABILITY_POWER);
                GuiHandler.setAdvancement(LibNames.ABILITY_RANGE, ModAttributes.ABILITY_RANGE);
                GuiHandler.setAdvancement(LibNames.ARMOR_PENETRATION, ModAttributes.ARMOR_PENETRATION);
                GuiHandler.setAdvancement(LibNames.ATTACK_SPEED, ModAttributes.ATTACK_SPEED);
                GuiHandler.setAdvancement(LibNames.CRITICAL_CHANCE, ModAttributes.CRITICAL_CHANCE);
                GuiHandler.setAdvancement(LibNames.KNOCKBACK, ModAttributes.KNOCKBACK);
                GuiHandler.setAdvancement(LibNames.DAMAGE, ModAttributes.DAMAGE);
                //
                GuiHandler.setAdvancement(LibNames.ANIMATED_STONE_GOLEM, ModAbilities.ANIMATED_STONE_GOLEM, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.DOME, ModAbilities.DOME, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.SHOCKWAVE, ModAbilities.SHOCKWAVE, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.TAUNT, ModAbilities.TAUNT, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.THORNY, ModAbilities.THORNY, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.WALL, ModAbilities.WALL, ModAbilities.TAUNT);
                GuiHandler.setAdvancement(LibNames.ELECTRIC_PULSE, ModAbilities.ELECTRIC_PULSE, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.ENERGIZE, ModAbilities.ENERGIZE, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.MAGNETIC_PULL, ModAbilities.MAGNETIC_PULL, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.POWER_DRAIN, ModAbilities.POWER_DRAIN, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.SHOCKING_AURA, ModAbilities.SHOCKING_AURA, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.VOLTAIC_SENTINEL, ModAbilities.VOLTAIC_SENTINEL, ModAbilities.SHOCKING_AURA);
                GuiHandler.setAdvancement(LibNames.BLAZING_AURA, ModAbilities.BLAZING_AURA, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.FLARES, ModAbilities.FLARES, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.HOME_STAR, ModAbilities.HOME_STAR, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.OVERHEAT, ModAbilities.OVERHEAT, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.RING_OF_FIRE, ModAbilities.RING_OF_FIRE, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.WARM_HEART, ModAbilities.WARM_HEART, ModAbilities.FLARES);
                GuiHandler.setAdvancement(LibNames.CHARM, ModAbilities.CHARM, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.HEAL_AURA, ModAbilities.HEAL_AURA, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.HEAL_OTHER, ModAbilities.HEAL_OTHER, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.HEAL_SELF, ModAbilities.HEAL_SELF, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.NEARBY_INVINCIBILITY, ModAbilities.NEARBY_INVINCIBILITY, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.POWER_BOOST, ModAbilities.POWER_BOOST, ModAbilities.CHARM);
                GuiHandler.setAdvancement(LibNames.HOVER, ModAbilities.HOVER, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.INVISIBILITY, ModAbilities.INVISIBILITY, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.PORTAL, ModAbilities.PORTAL, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.TELEPORT, ModAbilities.TELEPORT, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.UNSTABLE_PORTAL, ModAbilities.UNSTABLE_PORTAL, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.WARP, ModAbilities.WARP, ModAbilities.WARP);
                GuiHandler.setAdvancement(LibNames.DASH, ModAbilities.DASH, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.EXTRA_JUMP, ModAbilities.EXTRA_JUMP, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.FOG, ModAbilities.FOG, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.HASTEN, ModAbilities.HASTEN, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.SMASH, ModAbilities.SMASH, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.SPEED_BOOST, ModAbilities.SPEED_BOOST, ModAbilities.DASH);
                GuiHandler.setAdvancement(LibNames.BLEED, ModAbilities.BLEED, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.BLOOD_POOL, ModAbilities.BLOOD_POOL, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.CONTAMINATE, ModAbilities.CONTAMINATE, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.LIFE_STEAL, ModAbilities.LIFE_STEAL, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.SACRIFICE, ModAbilities.SACRIFICE, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.SYPHON, ModAbilities.SYPHON, ModAbilities.BLEED);
                GuiHandler.setAdvancement(LibNames.BLACK_HOLE, ModAbilities.BLACK_HOLE, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.GAS_CLOUD, ModAbilities.GAS_CLOUD, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.GLOOM, ModAbilities.GLOOM, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.GRASP, ModAbilities.GRASP, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.SHADOW, ModAbilities.SHADOW, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.SHADOW_JAB, ModAbilities.SHADOW_JAB, ModAbilities.SHADOW);
                GuiHandler.setAdvancement(LibNames.EXPLODE, ModAbilities.EXPLODE, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.FIREBALL, ModAbilities.FIREBALL, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.FIRE_SPIRIT, ModAbilities.FIRE_SPIRIT, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.FLAMING_BREATH, ModAbilities.FLAMING_BREATH, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.FLAMING_RAIN, ModAbilities.FLAMING_RAIN, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.FOCUS_FLAME, ModAbilities.FOCUS_FLAME, ModAbilities.FIRE_SPIRIT);
                GuiHandler.setAdvancement(LibNames.BARRAGE_WISPS, ModAbilities.BARRAGE_WISPS, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.FINAL_FLASH, ModAbilities.FINAL_FLASH, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.GLEAM_FLASH, ModAbilities.GLEAM_FLASH, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.LUMEN_WAVE, ModAbilities.LUMEN_WAVE, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.RADIANT_RAY, ModAbilities.RADIANT_RAY, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.SOLAR_LANCE, ModAbilities.SOLAR_LANCE, ModAbilities.RADIANT_RAY);
                GuiHandler.setAdvancement(LibNames.CRUSH, ModAbilities.CRUSH, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.PULL, ModAbilities.PULL, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.PUSH, ModAbilities.PUSH, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.SLASH, ModAbilities.SLASH, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.SUFFOCATE, ModAbilities.SUFFOCATE, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.UPDRAFT, ModAbilities.UPDRAFT, ModAbilities.SLASH);
                GuiHandler.setAdvancement(LibNames.BLACK_FLAME_BALL, ModAbilities.BLACK_FLAME_BALL, ModAbilities.BLACK_FLAME_BALL);
                GuiHandler.setAdvancement(LibNames.BLACK_SCOURING_FLAME, ModAbilities.BLACK_SCOURING_FLAME, ModAbilities.BLACK_FLAME_BALL);
                GuiHandler.setAdvancement(LibNames.BLACK_BLESSING_FLAME, ModAbilities.BLACK_BLESSING_FLAME, ModAbilities.BLACK_FLAME_BALL);
                GuiHandler.setAdvancement(LibNames.BLACK_RAGING_FLAME_BALL, ModAbilities.BLACK_RAGING_FLAME_BALL, ModAbilities.BLACK_FLAME_BALL);
                GuiHandler.setAdvancement(LibNames.BLACK_VOLLEY_FLAME, ModAbilities.BLACK_VOLLEY_FLAME, ModAbilities.BLACK_FLAME_BALL);
                GuiHandler.setAdvancement(LibNames.BLACK_FLARE_FLAME, ModAbilities.BLACK_FLARE_FLAME, ModAbilities.BLACK_FLAME_BALL);
                //
                GuiScreenSkillAdvancements window = new GuiScreenSkillAdvancements();
                GuiSkillAdvancementTab defense = window.addTab(new TextComponentTranslation(get("tab.defense.title")), SkillAdvancementTabType.BELOW, 0x65974B, 0);
                if (defense != null) {
                    GuiSkillAdvancementPage light = defense.addPage(new TextComponentTranslation(get("page.light.title")));
                    SkillAdvancementConditionSimple charm = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.charm.title")),
                                    new TextComponentTranslation(get("skill.charm.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.CHARM,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute explosion_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.explosion_resistance.title")),
                                        new TextComponentTranslation(get("skill.explosion_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.EXPLOSION_RESISTANCE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute damage_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage_resistance.title")),
                                        new TextComponentTranslation(get("skill.damage_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE_RESISTANCE,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback_resistance.title")),
                                        new TextComponentTranslation(get("skill.knockback_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute magic_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.magic_resistance.title")),
                                        new TextComponentTranslation(get("skill.magic_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.MAGIC_RESISTANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute heart_boost = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heart_boost.title")),
                                        new TextComponentTranslation(get("skill.heart_boost.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.HEART_BOOST,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute fire_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fire_resistance.title")),
                                        new TextComponentTranslation(get("skill.fire_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FIRE_RESISTANCE,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple heal_aura = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heal_aura.title")),
                                        new TextComponentTranslation(get("skill.heal_aura.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.HEAL_AURA,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple power_boost = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.power_boost.title")),
                                        new TextComponentTranslation(get("skill.power_boost.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.POWER_BOOST,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple heal_other = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heal_other.title")),
                                        new TextComponentTranslation(get("skill.heal_other.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.HEAL_OTHER,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple heal_self = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heal_self.title")),
                                        new TextComponentTranslation(get("skill.heal_self.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.HEAL_SELF,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple nearby_invisibility = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.nearby_invisibility.title")),
                                        new TextComponentTranslation(get("skill.nearby_invisibility.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.NEARBY_INVINCIBILITY,
                                        false
                                ),
                                6, 1
                        );
                        //Requirements
                        heal_aura.addCondition(charm);
                        heal_aura.addCondition(new SkillAdvancementConditionNotOrOverride(power_boost, nearby_invisibility));
                        power_boost.addCondition(charm);
                        power_boost.addCondition(new SkillAdvancementConditionNotOrOverride(heal_aura, nearby_invisibility));
                        heal_other.addCondition(new SkillAdvancementConditionOr(heal_aura, power_boost));
                        heal_other.addCondition(new SkillAdvancementConditionNotOrOverride(heal_self, nearby_invisibility));
                        heal_other.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(heal_other, nearby_invisibility, heal_aura, power_boost));
                        heal_self.addCondition(new SkillAdvancementConditionOr(heal_aura, power_boost));
                        heal_self.addCondition(new SkillAdvancementConditionNotOrOverride(heal_other, nearby_invisibility));
                        heal_self.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(heal_self, nearby_invisibility, heal_aura, power_boost));
                        nearby_invisibility.addCondition(new SkillAdvancementConditionOr(heal_self, heal_other));
                        explosion_resistance.addCondition(charm);
                        damage_resistance.addCondition(charm);
                        knockback_resistance.addCondition(charm);
                        magic_resistance.addCondition(new SkillAdvancementConditionOr(heal_aura, power_boost));
                        heart_boost.addCondition(new SkillAdvancementConditionOr(heal_aura, power_boost));
                        fire_resistance.addCondition(new SkillAdvancementConditionOr(heal_aura, power_boost));
                        //Altar Requirements
                        magic_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        heart_boost.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fire_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        charm.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        heal_aura.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        power_boost.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        heal_other.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        heal_self.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        nearby_invisibility.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = light.addAdvancement(charm);
                        GuiSkillAdvancement gui1 = light.addAdvancement(heal_aura, power_boost);
                        GuiSkillAdvancement gui2 = light.addAdvancement(heal_other, heal_self);
                        GuiSkillAdvancement gui3 = light.addAdvancement(nearby_invisibility);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        light.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        light.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }
                    GuiSkillAdvancementPage earth = defense.addPage(new TextComponentTranslation(get("page.earth.title")));
                    SkillAdvancementConditionSimple taunt = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.taunt.title")),
                                    new TextComponentTranslation(get("skill.taunt.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.TAUNT,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute explosion_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.explosion_resistance.title")),
                                        new TextComponentTranslation(get("skill.explosion_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.EXPLOSION_RESISTANCE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute damage_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage_resistance.title")),
                                        new TextComponentTranslation(get("skill.damage_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE_RESISTANCE,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback_resistance.title")),
                                        new TextComponentTranslation(get("skill.knockback_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute magic_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.magic_resistance.title")),
                                        new TextComponentTranslation(get("skill.magic_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.MAGIC_RESISTANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute heart_boost = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heart_boost.title")),
                                        new TextComponentTranslation(get("skill.heart_boost.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.HEART_BOOST,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute fire_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fire_resistance.title")),
                                        new TextComponentTranslation(get("skill.fire_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FIRE_RESISTANCE,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple wall = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.wall.title")),
                                        new TextComponentTranslation(get("skill.wall.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.WALL,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple dome = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.dome.title")),
                                        new TextComponentTranslation(get("skill.dome.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.DOME,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple thorny = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.thorny.title")),
                                        new TextComponentTranslation(get("skill.thorny.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.THORNY,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple shockwave = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.shockwave.title")),
                                        new TextComponentTranslation(get("skill.shockwave.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.SHOCKWAVE,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple animatedStoneGolem = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.animated_stone_golem.title")),
                                        new TextComponentTranslation(get("skill.animated_stone_golem.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.ANIMATED_STONE_GOLEM,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        wall.addCondition(taunt);
                        wall.addCondition(new SkillAdvancementConditionNotOrOverride(dome, animatedStoneGolem));
                        dome.addCondition(taunt);
                        dome.addCondition(new SkillAdvancementConditionNotOrOverride(wall, animatedStoneGolem));
                        thorny.addCondition(new SkillAdvancementConditionOr(wall, dome));
                        thorny.addCondition(new SkillAdvancementConditionNotOrOverride(shockwave, animatedStoneGolem));
                        thorny.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(thorny, animatedStoneGolem, wall, dome));
                        shockwave.addCondition(new SkillAdvancementConditionOr(wall, dome));
                        shockwave.addCondition(new SkillAdvancementConditionNotOrOverride(thorny, animatedStoneGolem));
                        shockwave.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(shockwave, animatedStoneGolem, wall, dome));
                        animatedStoneGolem.addCondition(new SkillAdvancementConditionOr(shockwave, thorny));
                        explosion_resistance.addCondition(taunt);
                        damage_resistance.addCondition(taunt);
                        knockback_resistance.addCondition(taunt);
                        magic_resistance.addCondition(new SkillAdvancementConditionOr(wall, dome));
                        heart_boost.addCondition(new SkillAdvancementConditionOr(wall, dome));
                        fire_resistance.addCondition(new SkillAdvancementConditionOr(wall, dome));
                        //Altar Requirements
                        magic_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        heart_boost.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fire_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        taunt.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        wall.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        dome.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        thorny.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        shockwave.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        animatedStoneGolem.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = earth.addAdvancement(taunt);
                        GuiSkillAdvancement gui1 = earth.addAdvancement(wall, dome);
                        GuiSkillAdvancement gui2 = earth.addAdvancement(thorny, shockwave);
                        GuiSkillAdvancement gui3 = earth.addAdvancement(animatedStoneGolem);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        earth.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        earth.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }
                    GuiSkillAdvancementPage electric = defense.addPage(new TextComponentTranslation(get("page.electric.title")));
                    SkillAdvancementConditionSimple shocking_aura = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.shocking_aura.title")),
                                    new TextComponentTranslation(get("skill.shocking_aura.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.SHOCKING_AURA,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute explosion_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.explosion_resistance.title")),
                                        new TextComponentTranslation(get("skill.explosion_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.EXPLOSION_RESISTANCE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute damage_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage_resistance.title")),
                                        new TextComponentTranslation(get("skill.damage_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE_RESISTANCE,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback_resistance.title")),
                                        new TextComponentTranslation(get("skill.knockback_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute magic_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.magic_resistance.title")),
                                        new TextComponentTranslation(get("skill.magic_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.MAGIC_RESISTANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute heart_boost = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heart_boost.title")),
                                        new TextComponentTranslation(get("skill.heart_boost.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.HEART_BOOST,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute fire_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fire_resistance.title")),
                                        new TextComponentTranslation(get("skill.fire_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FIRE_RESISTANCE,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple electricPulse = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.electric_pulse.title")),
                                        new TextComponentTranslation(get("skill.electric_pulse.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.ELECTRIC_PULSE,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple magneticPull = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.magnetic_pull.title")),
                                        new TextComponentTranslation(get("skill.magnetic_pull.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.MAGNETIC_PULL,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple powerDrain = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.power_drain.title")),
                                        new TextComponentTranslation(get("skill.power_drain.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.POWER_DRAIN,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple energize = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.energize.title")),
                                        new TextComponentTranslation(get("skill.energize.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.ENERGIZE,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple voltaicSentinel = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.voltaic_sentinel.title")),
                                        new TextComponentTranslation(get("skill.voltaic_sentinel.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.VOLTAIC_SENTINEL,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        electricPulse.addCondition(shocking_aura);
                        electricPulse.addCondition(new SkillAdvancementConditionNotOrOverride(magneticPull, voltaicSentinel));
                        magneticPull.addCondition(shocking_aura);
                        magneticPull.addCondition(new SkillAdvancementConditionNotOrOverride(electricPulse, voltaicSentinel));
                        powerDrain.addCondition(new SkillAdvancementConditionOr(electricPulse, magneticPull));
                        powerDrain.addCondition(new SkillAdvancementConditionNotOrOverride(energize, voltaicSentinel));
                        powerDrain.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(powerDrain, voltaicSentinel, electricPulse, magneticPull));
                        energize.addCondition(new SkillAdvancementConditionOr(electricPulse, magneticPull));
                        energize.addCondition(new SkillAdvancementConditionNotOrOverride(powerDrain, voltaicSentinel));
                        energize.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(energize, voltaicSentinel, electricPulse, magneticPull));
                        voltaicSentinel.addCondition(new SkillAdvancementConditionOr(energize, powerDrain));
                        explosion_resistance.addCondition(shocking_aura);
                        damage_resistance.addCondition(shocking_aura);
                        knockback_resistance.addCondition(shocking_aura);
                        magic_resistance.addCondition(new SkillAdvancementConditionOr(electricPulse, magneticPull));
                        heart_boost.addCondition(new SkillAdvancementConditionOr(electricPulse, magneticPull));
                        fire_resistance.addCondition(new SkillAdvancementConditionOr(electricPulse, magneticPull));
                        //Altar Requirements
                        magic_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        heart_boost.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fire_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        shocking_aura.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        electricPulse.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        magneticPull.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        powerDrain.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        energize.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        voltaicSentinel.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = electric.addAdvancement(shocking_aura);
                        GuiSkillAdvancement gui1 = electric.addAdvancement(electricPulse, magneticPull);
                        GuiSkillAdvancement gui2 = electric.addAdvancement(powerDrain, energize);
                        GuiSkillAdvancement gui3 = electric.addAdvancement(voltaicSentinel);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        electric.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        electric.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }
                    GuiSkillAdvancementPage fire = defense.addPage(new TextComponentTranslation(get("page.fire.title")));
                    SkillAdvancementConditionSimple flares = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.flares.title")),
                                    new TextComponentTranslation(get("skill.flares.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.FLARES,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute explosion_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.explosion_resistance.title")),
                                        new TextComponentTranslation(get("skill.explosion_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.EXPLOSION_RESISTANCE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute damage_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage_resistance.title")),
                                        new TextComponentTranslation(get("skill.damage_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE_RESISTANCE,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback_resistance.title")),
                                        new TextComponentTranslation(get("skill.knockback_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute magic_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.magic_resistance.title")),
                                        new TextComponentTranslation(get("skill.magic_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.MAGIC_RESISTANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute heart_boost = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.heart_boost.title")),
                                        new TextComponentTranslation(get("skill.heart_boost.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.HEART_BOOST,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute fire_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fire_resistance.title")),
                                        new TextComponentTranslation(get("skill.fire_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FIRE_RESISTANCE,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple blazing_aura = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.blazing_aura.title")),
                                        new TextComponentTranslation(get("skill.blazing_aura.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLAZING_AURA,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple ring_of_fire = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ring_of_fire.title")),
                                        new TextComponentTranslation(get("skill.ring_of_fire.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.RING_OF_FIRE,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple overheat = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.overheat.title")),
                                        new TextComponentTranslation(get("skill.overheat.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.OVERHEAT,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple warm_heart = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.warm_heart.title")),
                                        new TextComponentTranslation(get("skill.warm_heart.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.WARM_HEART,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple home_star = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.home_star.title")),
                                        new TextComponentTranslation(get("skill.home_star.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.HOME_STAR,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        blazing_aura.addCondition(flares);
                        blazing_aura.addCondition(new SkillAdvancementConditionNotOrOverride(ring_of_fire, home_star));
                        ring_of_fire.addCondition(flares);
                        ring_of_fire.addCondition(new SkillAdvancementConditionNotOrOverride(blazing_aura, home_star));
                        overheat.addCondition(new SkillAdvancementConditionOr(blazing_aura, ring_of_fire));
                        overheat.addCondition(new SkillAdvancementConditionNotOrOverride(warm_heart, home_star));
                        overheat.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(overheat, home_star, blazing_aura, ring_of_fire));
                        warm_heart.addCondition(new SkillAdvancementConditionOr(blazing_aura, ring_of_fire));
                        warm_heart.addCondition(new SkillAdvancementConditionNotOrOverride(overheat, home_star));
                        warm_heart.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(warm_heart, home_star, blazing_aura, ring_of_fire));
                        home_star.addCondition(new SkillAdvancementConditionOr(warm_heart, overheat));
                        explosion_resistance.addCondition(flares);
                        damage_resistance.addCondition(flares);
                        knockback_resistance.addCondition(flares);
                        magic_resistance.addCondition(new SkillAdvancementConditionOr(blazing_aura, ring_of_fire));
                        heart_boost.addCondition(new SkillAdvancementConditionOr(blazing_aura, ring_of_fire));
                        fire_resistance.addCondition(new SkillAdvancementConditionOr(blazing_aura, ring_of_fire));
                        //Altar Requirements
                        magic_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        heart_boost.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fire_resistance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        flares.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        blazing_aura.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        ring_of_fire.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        overheat.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        warm_heart.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        home_star.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = fire.addAdvancement(flares);
                        GuiSkillAdvancement gui1 = fire.addAdvancement(blazing_aura, ring_of_fire);
                        GuiSkillAdvancement gui2 = fire.addAdvancement(overheat, warm_heart);
                        GuiSkillAdvancement gui3 = fire.addAdvancement(home_star);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        fire.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        fire.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }

                    if (CommonConfig.getSyncValues().advancement.oneTreePerClass) {
                        charm.addCondition(new SkillAdvancementConditionNot(taunt));
                        charm.addCondition(new SkillAdvancementConditionNot(shocking_aura));
                        charm.addCondition(new SkillAdvancementConditionNot(flares));
                        taunt.addCondition(new SkillAdvancementConditionNot(charm));
                        taunt.addCondition(new SkillAdvancementConditionNot(shocking_aura));
                        taunt.addCondition(new SkillAdvancementConditionNot(flares));
                        shocking_aura.addCondition(new SkillAdvancementConditionNot(taunt));
                        shocking_aura.addCondition(new SkillAdvancementConditionNot(charm));
                        shocking_aura.addCondition(new SkillAdvancementConditionNot(flares));
                        flares.addCondition(new SkillAdvancementConditionNot(taunt));
                        flares.addCondition(new SkillAdvancementConditionNot(shocking_aura));
                        flares.addCondition(new SkillAdvancementConditionNot(charm));
                    }
                }
                GuiSkillAdvancementTab mobility = window.addTab(new TextComponentTranslation(get("tab.mobility.title")), SkillAdvancementTabType.BELOW, 0x329CA2, 1);
                if (mobility != null) {
                    GuiSkillAdvancementPage wind = mobility.addPage(new TextComponentTranslation(get("page.wind.title")));
                    SkillAdvancementConditionSimple dash = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.dash.title")),
                                    new TextComponentTranslation(get("skill.dash.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.DASH,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute jump_height = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.jump_height.title")),
                                        new TextComponentTranslation(get("skill.jump_height.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.JUMP_HEIGHT,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.speed.title")),
                                        new TextComponentTranslation(get("skill.speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute fall_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fall_resistance.title")),
                                        new TextComponentTranslation(get("skill.fall_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FALL_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute endurance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.endurance.title")),
                                        new TextComponentTranslation(get("skill.endurance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ENDURANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute stealth_damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.stealth_damage.title")),
                                        new TextComponentTranslation(get("skill.stealth_damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.STEALTH_DAMAGE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute swim_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.swim_speed.title")),
                                        new TextComponentTranslation(get("skill.swim_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.SWIM_SPEED,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple extra_jump = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.extra_jump.title")),
                                        new TextComponentTranslation(get("skill.extra_jump.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.EXTRA_JUMP,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple fog = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fog.title")),
                                        new TextComponentTranslation(get("skill.fog.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.FOG,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple smash = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.smash.title")),
                                        new TextComponentTranslation(get("skill.smash.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.SMASH,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple hasten = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.hasten.title")),
                                        new TextComponentTranslation(get("skill.hasten.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.HASTEN,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple speedBoost = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.speed_boost.title")),
                                        new TextComponentTranslation(get("skill.speed_boost.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.SPEED_BOOST,
                                        false
                                ),
                                6, 1
                        );
                        //Requirements
                        extra_jump.addCondition(dash);
                        extra_jump.addCondition(new SkillAdvancementConditionNotOrOverride(fog, speedBoost));
                        fog.addCondition(dash);
                        fog.addCondition(new SkillAdvancementConditionNotOrOverride(extra_jump, speedBoost));
                        smash.addCondition(new SkillAdvancementConditionOr(extra_jump, fog));
                        smash.addCondition(new SkillAdvancementConditionNotOrOverride(hasten, speedBoost));
                        smash.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(smash, speedBoost, extra_jump, fog));
                        hasten.addCondition(new SkillAdvancementConditionOr(extra_jump, fog));
                        hasten.addCondition(new SkillAdvancementConditionNotOrOverride(smash, speedBoost));
                        hasten.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(hasten, speedBoost, extra_jump, fog));
                        speedBoost.addCondition(new SkillAdvancementConditionOr(hasten, smash));
                        jump_height.addCondition(dash);
                        speed.addCondition(dash);
                        fall_resistance.addCondition(dash);
                        endurance.addCondition(new SkillAdvancementConditionOr(extra_jump, fog));
                        stealth_damage.addCondition(new SkillAdvancementConditionOr(extra_jump, fog));
                        swim_speed.addCondition(new SkillAdvancementConditionOr(extra_jump, fog));
                        //Altar Requirements
                        endurance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        stealth_damage.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        swim_speed.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        dash.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        extra_jump.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fog.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        smash.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        hasten.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        speedBoost.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = wind.addAdvancement(dash);
                        GuiSkillAdvancement gui1 = wind.addAdvancement(extra_jump, fog);
                        GuiSkillAdvancement gui2 = wind.addAdvancement(smash, hasten);
                        GuiSkillAdvancement gui3 = wind.addAdvancement(speedBoost);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        wind.addAdvancement(jump_height, speed, fall_resistance);
                        wind.addAdvancement(endurance, stealth_damage, swim_speed);
                    }
                    GuiSkillAdvancementPage ender = mobility.addPage(new TextComponentTranslation(get("page.void.title")));
                    SkillAdvancementConditionSimple warp = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.warp.title")),
                                    new TextComponentTranslation(get("skill.warp.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.WARP,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute jump_height = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.jump_height.title")),
                                        new TextComponentTranslation(get("skill.jump_height.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.JUMP_HEIGHT,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.speed.title")),
                                        new TextComponentTranslation(get("skill.speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute fall_resistance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fall_resistance.title")),
                                        new TextComponentTranslation(get("skill.fall_resistance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.FALL_RESISTANCE,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute endurance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.endurance.title")),
                                        new TextComponentTranslation(get("skill.endurance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ENDURANCE,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute stealth_damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.stealth_damage.title")),
                                        new TextComponentTranslation(get("skill.stealth_damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.STEALTH_DAMAGE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute swim_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.swim_speed.title")),
                                        new TextComponentTranslation(get("skill.swim_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.SWIM_SPEED,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple invisibility = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.invisibility.title")),
                                        new TextComponentTranslation(get("skill.invisibility.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.INVISIBILITY,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple hover = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.hover.title")),
                                        new TextComponentTranslation(get("skill.hover.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.HOVER,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple unstable_portal = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.unstable_portal.title")),
                                        new TextComponentTranslation(get("skill.unstable_portal.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.UNSTABLE_PORTAL,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple portal = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.portal.title")),
                                        new TextComponentTranslation(get("skill.portal.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.PORTAL,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple teleport = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.teleport.title")),
                                        new TextComponentTranslation(get("skill.teleport.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.TELEPORT,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        invisibility.addCondition(warp);
                        invisibility.addCondition(new SkillAdvancementConditionNotOrOverride(hover, teleport));
                        hover.addCondition(warp);
                        hover.addCondition(new SkillAdvancementConditionNotOrOverride(invisibility, teleport));
                        unstable_portal.addCondition(new SkillAdvancementConditionOr(invisibility, hover));
                        unstable_portal.addCondition(new SkillAdvancementConditionNotOrOverride(portal, teleport));
                        unstable_portal.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(unstable_portal, teleport, invisibility, hover));
                        portal.addCondition(new SkillAdvancementConditionOr(invisibility, hover));
                        portal.addCondition(new SkillAdvancementConditionNotOrOverride(unstable_portal, teleport));
                        portal.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(portal, teleport, invisibility, hover));
                        teleport.addCondition(new SkillAdvancementConditionOr(portal, unstable_portal));
                        jump_height.addCondition(warp);
                        speed.addCondition(warp);
                        fall_resistance.addCondition(warp);
                        endurance.addCondition(new SkillAdvancementConditionOr(invisibility, hover));
                        stealth_damage.addCondition(new SkillAdvancementConditionOr(invisibility, hover));
                        swim_speed.addCondition(new SkillAdvancementConditionOr(invisibility, hover));
                        //Altar Requirements
                        endurance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        stealth_damage.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        swim_speed.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        warp.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        invisibility.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        hover.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        unstable_portal.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        portal.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        teleport.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = ender.addAdvancement(warp);
                        GuiSkillAdvancement gui1 = ender.addAdvancement(invisibility, hover);
                        GuiSkillAdvancement gui2 = ender.addAdvancement(unstable_portal, portal);
                        GuiSkillAdvancement gui3 = ender.addAdvancement(teleport);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        ender.addAdvancement(jump_height, speed, fall_resistance);
                        ender.addAdvancement(endurance, stealth_damage, swim_speed);
                    }

                    if (CommonConfig.getSyncValues().advancement.oneTreePerClass) {
                        dash.addCondition(new SkillAdvancementConditionNot(warp));
                        warp.addCondition(new SkillAdvancementConditionNot(dash));
                    }
                }
                GuiSkillAdvancementTab offense = window.addTab(new TextComponentTranslation(get("tab.offense.title")), SkillAdvancementTabType.BELOW, 0xAF3B3E, 2);
                if (offense != null) {
                    GuiSkillAdvancementPage ender = offense.addPage(new TextComponentTranslation(get("page.void.title")));
                    SkillAdvancementConditionSimple shadow = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.shadow.title")),
                                    new TextComponentTranslation(get("skill.shadow.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.SHADOW,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple gloom = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.gloom.title")),
                                        new TextComponentTranslation(get("skill.gloom.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.GLOOM,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple shadow_jab = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.shadow_jab.title")),
                                        new TextComponentTranslation(get("skill.shadow_jab.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.SHADOW_JAB,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple gas_cloud = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.gas_cloud.title")),
                                        new TextComponentTranslation(get("skill.gas_cloud.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.GAS_CLOUD,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple grasp = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.grasp.title")),
                                        new TextComponentTranslation(get("skill.grasp.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.GRASP,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple black_hole = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_hole.title")),
                                        new TextComponentTranslation(get("skill.black_hole.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.BLACK_HOLE,
                                        false
                                ),
                                6, 1
                        );
                        //Requirements
                        gloom.addCondition(shadow);
                        gloom.addCondition(new SkillAdvancementConditionNotOrOverride(shadow_jab, black_hole));
                        shadow_jab.addCondition(shadow);
                        shadow_jab.addCondition(new SkillAdvancementConditionNotOrOverride(gloom, black_hole));
                        gas_cloud.addCondition(new SkillAdvancementConditionOr(gloom, shadow_jab));
                        gas_cloud.addCondition(new SkillAdvancementConditionNotOrOverride(grasp, black_hole));
                        gas_cloud.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(gas_cloud, black_hole, gloom, shadow_jab));
                        grasp.addCondition(new SkillAdvancementConditionOr(gloom, shadow_jab));
                        grasp.addCondition(new SkillAdvancementConditionNotOrOverride(gas_cloud, black_hole));
                        grasp.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(grasp, black_hole, gloom, shadow_jab));
                        black_hole.addCondition(new SkillAdvancementConditionOr(grasp, gas_cloud));
                        damage.addCondition(shadow);
                        attack_speed.addCondition(shadow);
                        knockback.addCondition(shadow);
                        ability_power.addCondition(new SkillAdvancementConditionOr(gloom, shadow_jab));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(gloom, shadow_jab));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(gloom, shadow_jab));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        shadow.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        gloom.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        shadow_jab.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        gas_cloud.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        grasp.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        black_hole.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = ender.addAdvancement(shadow);
                        GuiSkillAdvancement gui1 = ender.addAdvancement(gloom, shadow_jab);
                        GuiSkillAdvancement gui2 = ender.addAdvancement(gas_cloud, grasp);
                        GuiSkillAdvancement gui3 = ender.addAdvancement(black_hole);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        ender.addAdvancement(damage, attack_speed, knockback);
                        ender.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage blood = offense.addPage(new TextComponentTranslation(get("page.blood.title")));
                    SkillAdvancementConditionSimple bleed = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.bleed.title")),
                                    new TextComponentTranslation(get("skill.bleed.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.BLEED,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple blood_pool = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.blood_pool.title")),
                                        new TextComponentTranslation(get("skill.blood_pool.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLOOD_POOL,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple contaminate = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.contaminate.title")),
                                        new TextComponentTranslation(get("skill.contaminate.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.CONTAMINATE,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple life_steal = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.life_steal.title")),
                                        new TextComponentTranslation(get("skill.life_steal.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.LIFE_STEAL,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple syphon = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.syphon.title")),
                                        new TextComponentTranslation(get("skill.syphon.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.SYPHON,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple sacrifice = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.sacrifice.title")),
                                        new TextComponentTranslation(get("skill.sacrifice.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.SACRIFICE,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        blood_pool.addCondition(bleed);
                        blood_pool.addCondition(new SkillAdvancementConditionNotOrOverride(contaminate, sacrifice));
                        contaminate.addCondition(bleed);
                        contaminate.addCondition(new SkillAdvancementConditionNotOrOverride(blood_pool, sacrifice));
                        life_steal.addCondition(new SkillAdvancementConditionOr(blood_pool, contaminate));
                        life_steal.addCondition(new SkillAdvancementConditionNotOrOverride(syphon, sacrifice));
                        life_steal.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(life_steal, sacrifice, blood_pool, contaminate));
                        syphon.addCondition(new SkillAdvancementConditionOr(blood_pool, contaminate));
                        syphon.addCondition(new SkillAdvancementConditionNotOrOverride(life_steal, sacrifice));
                        syphon.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(syphon, sacrifice, blood_pool, contaminate));
                        sacrifice.addCondition(new SkillAdvancementConditionOr(syphon, life_steal));
                        damage.addCondition(bleed);
                        attack_speed.addCondition(bleed);
                        knockback.addCondition(bleed);
                        ability_power.addCondition(new SkillAdvancementConditionOr(blood_pool, contaminate));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(blood_pool, contaminate));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(blood_pool, contaminate));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        bleed.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        blood_pool.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        contaminate.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        life_steal.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        syphon.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        sacrifice.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = blood.addAdvancement(bleed);
                        GuiSkillAdvancement gui1 = blood.addAdvancement(blood_pool, contaminate);
                        GuiSkillAdvancement gui2 = blood.addAdvancement(life_steal, syphon);
                        GuiSkillAdvancement gui3 = blood.addAdvancement(sacrifice);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        blood.addAdvancement(damage, attack_speed, knockback);
                        blood.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage wind = offense.addPage(new TextComponentTranslation(get("page.wind.title")));
                    SkillAdvancementConditionSimple slash = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.slash.title")),
                                    new TextComponentTranslation(get("skill.slash.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.SLASH,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple push = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.push.title")),
                                        new TextComponentTranslation(get("skill.push.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.PUSH,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple pull = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.pull.title")),
                                        new TextComponentTranslation(get("skill.pull.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.PULL,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple crush = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.crush.title")),
                                        new TextComponentTranslation(get("skill.crush.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.CRUSH,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple updraft = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.updraft.title")),
                                        new TextComponentTranslation(get("skill.updraft.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.UPDRAFT,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple suffocate = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.suffocate.title")),
                                        new TextComponentTranslation(get("skill.suffocate.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.SUFFOCATE,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        push.addCondition(slash);
                        push.addCondition(new SkillAdvancementConditionNotOrOverride(pull, suffocate));
                        pull.addCondition(slash);
                        pull.addCondition(new SkillAdvancementConditionNotOrOverride(push, suffocate));
                        crush.addCondition(new SkillAdvancementConditionOr(push, pull));
                        crush.addCondition(new SkillAdvancementConditionNotOrOverride(updraft, suffocate));
                        crush.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(crush, suffocate, push, pull));
                        updraft.addCondition(new SkillAdvancementConditionOr(push, pull));
                        updraft.addCondition(new SkillAdvancementConditionNotOrOverride(crush, suffocate));
                        updraft.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(updraft, suffocate, push, pull));
                        suffocate.addCondition(new SkillAdvancementConditionOr(updraft, crush));
                        damage.addCondition(slash);
                        attack_speed.addCondition(slash);
                        knockback.addCondition(slash);
                        ability_power.addCondition(new SkillAdvancementConditionOr(push, pull));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(push, pull));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(push, pull));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        slash.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        push.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        pull.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        crush.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        updraft.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        suffocate.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = wind.addAdvancement(slash);
                        GuiSkillAdvancement gui1 = wind.addAdvancement(push, pull);
                        GuiSkillAdvancement gui2 = wind.addAdvancement(crush, updraft);
                        GuiSkillAdvancement gui3 = wind.addAdvancement(suffocate);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        wind.addAdvancement(damage, attack_speed, knockback);
                        wind.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage fire = offense.addPage(new TextComponentTranslation(get("page.fire.title")));
                    SkillAdvancementConditionSimple fire_spirit = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.fire_spirit.title")),
                                    new TextComponentTranslation(get("skill.fire_spirit.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.FIRE_SPIRIT,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple flaming_breath = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.flaming_breath.title")),
                                        new TextComponentTranslation(get("skill.flaming_breath.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.FLAMING_BREATH,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple flaming_rain = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.flaming_rain.title")),
                                        new TextComponentTranslation(get("skill.flaming_rain.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.FLAMING_RAIN,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple focus_flame = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.focus_flame.title")),
                                        new TextComponentTranslation(get("skill.focus_flame.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.FOCUS_FLAME,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple fireball = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.fireball.title")),
                                        new TextComponentTranslation(get("skill.fireball.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.FIREBALL,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple explode = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.explode.title")),
                                        new TextComponentTranslation(get("skill.explode.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.EXPLODE,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        flaming_breath.addCondition(fire_spirit);
                        flaming_breath.addCondition(new SkillAdvancementConditionNotOrOverride(flaming_rain, explode));
                        flaming_rain.addCondition(fire_spirit);
                        flaming_rain.addCondition(new SkillAdvancementConditionNotOrOverride(flaming_breath, explode));
                        focus_flame.addCondition(new SkillAdvancementConditionOr(flaming_breath, flaming_rain));
                        focus_flame.addCondition(new SkillAdvancementConditionNotOrOverride(fireball, explode));
                        focus_flame.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(focus_flame, explode, flaming_breath, flaming_rain));
                        fireball.addCondition(new SkillAdvancementConditionOr(flaming_breath, flaming_rain));
                        fireball.addCondition(new SkillAdvancementConditionNotOrOverride(focus_flame, explode));
                        fireball.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(fireball, explode, flaming_breath, flaming_rain));
                        explode.addCondition(new SkillAdvancementConditionOr(fireball, focus_flame));

                        damage.addCondition(fire_spirit);
                        attack_speed.addCondition(fire_spirit);
                        knockback.addCondition(fire_spirit);
                        ability_power.addCondition(new SkillAdvancementConditionOr(flaming_breath, flaming_rain));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(flaming_breath, flaming_rain));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(flaming_breath, flaming_rain));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        fire_spirit.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        flaming_breath.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        flaming_rain.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        focus_flame.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        fireball.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        explode.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = fire.addAdvancement(fire_spirit);
                        GuiSkillAdvancement gui1 = fire.addAdvancement(flaming_breath, flaming_rain);
                        GuiSkillAdvancement gui2 = fire.addAdvancement(focus_flame, fireball);
                        GuiSkillAdvancement gui3 = fire.addAdvancement(explode);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        fire.addAdvancement(damage, attack_speed, knockback);
                        fire.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage light = offense.addPage(new TextComponentTranslation(get("page.light.title")));
                    SkillAdvancementConditionSimple radiant_ray = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.radiant_ray.title")),
                                    new TextComponentTranslation(get("skill.radiant_ray.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.RADIANT_RAY,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple lumen_wave = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.lumen_wave.title")),
                                        new TextComponentTranslation(get("skill.lumen_wave.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.LUMEN_WAVE,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple gleam_flash = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.gleam_flash.title")),
                                        new TextComponentTranslation(get("skill.gleam_flash.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.GLEAM_FLASH,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple solar_lance = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.solar_lance.title")),
                                        new TextComponentTranslation(get("skill.solar_lance.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.SOLAR_LANCE,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple brown_out = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.barrage_wisp.title")),
                                        new TextComponentTranslation(get("skill.barrage_wisp.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BARRAGE_WISPS,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple superstar = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.final_flash.title")),
                                        new TextComponentTranslation(get("skill.final_flash.description")),
                                        SkillAdvancementInfo.Frame.SPECIAL,
                                        ModAbilities.FINAL_FLASH,
                                        false
                                ),
                                6, 1
                        );

                        //Requirements
                        lumen_wave.addCondition(radiant_ray);
                        lumen_wave.addCondition(new SkillAdvancementConditionNotOrOverride(gleam_flash, superstar));
                        gleam_flash.addCondition(radiant_ray);
                        gleam_flash.addCondition(new SkillAdvancementConditionNotOrOverride(lumen_wave, superstar));
                        solar_lance.addCondition(new SkillAdvancementConditionOr(lumen_wave, gleam_flash));
                        solar_lance.addCondition(new SkillAdvancementConditionNotOrOverride(brown_out, superstar));
                        solar_lance.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(solar_lance, superstar, lumen_wave, gleam_flash));
                        brown_out.addCondition(new SkillAdvancementConditionOr(lumen_wave, gleam_flash));
                        brown_out.addCondition(new SkillAdvancementConditionNotOrOverride(solar_lance, superstar));
                        brown_out.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(brown_out, superstar, lumen_wave, gleam_flash));
                        superstar.addCondition(new SkillAdvancementConditionOr(brown_out, solar_lance));

                        damage.addCondition(radiant_ray);
                        attack_speed.addCondition(radiant_ray);
                        knockback.addCondition(radiant_ray);
                        ability_power.addCondition(new SkillAdvancementConditionOr(lumen_wave, gleam_flash));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(lumen_wave, gleam_flash));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(lumen_wave, gleam_flash));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        radiant_ray.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        lumen_wave.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        gleam_flash.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        solar_lance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        brown_out.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_2));
                        superstar.addCondition(new SkillAdvancementConditionAltarUltimate());
                        //GUI
                        GuiSkillAdvancement gui0 = light.addAdvancement(radiant_ray);
                        GuiSkillAdvancement gui1 = light.addAdvancement(lumen_wave, gleam_flash);
                        GuiSkillAdvancement gui2 = light.addAdvancement(solar_lance, brown_out);
                        GuiSkillAdvancement gui3 = light.addAdvancement(superstar);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        light.addAdvancement(damage, attack_speed, knockback);
                        light.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage black_flame = offense.addPage(new TextComponentTranslation(get("page.black_flame.title")));
                    SkillAdvancementConditionSimple black_flame_ball = new SkillAdvancementConditionSimple(
                            new SkillAdvancementInfo(
                                    new TextComponentTranslation(get("skill.black_flame_ball.title")),
                                    new TextComponentTranslation(get("skill.black_flame_ball.description")),
                                    SkillAdvancementInfo.Frame.NORMAL,
                                    ModAbilities.BLACK_FLAME_BALL,
                                    false
                            ),
                            0, 1
                    );
                    {
                        //Attribute
                        SkillAdvancementConditionAttribute damage = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.damage.title")),
                                        new TextComponentTranslation(get("skill.damage.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.DAMAGE,
                                        false
                                ),
                                0, 5
                        );
                        SkillAdvancementConditionAttribute attack_speed = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.attack_speed.title")),
                                        new TextComponentTranslation(get("skill.attack_speed.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ATTACK_SPEED,
                                        false
                                ),
                                0, 6
                        );
                        SkillAdvancementConditionAttribute knockback = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.knockback.title")),
                                        new TextComponentTranslation(get("skill.knockback.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.KNOCKBACK,
                                        false
                                ),
                                0, 7
                        );
                        SkillAdvancementConditionAttribute ability_power = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.ability_power.title")),
                                        new TextComponentTranslation(get("skill.ability_power.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ABILITY_POWER,
                                        false
                                ),
                                2, 5
                        );
                        SkillAdvancementConditionAttribute critical_chance = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.critical_chance.title")),
                                        new TextComponentTranslation(get("skill.critical_chance.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.CRITICAL_CHANCE,
                                        false
                                ),
                                2, 6
                        );
                        SkillAdvancementConditionAttribute armor_penetration = new SkillAdvancementConditionAttribute(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.armor_penetration.title")),
                                        new TextComponentTranslation(get("skill.armor_penetration.description")),
                                        SkillAdvancementInfo.Frame.NORMAL,
                                        ModAttributes.ARMOR_PENETRATION,
                                        false
                                ),
                                2, 7
                        );
                        //Ability
                        SkillAdvancementConditionSimple black_scouring_flame = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_scouring_flame.title")),
                                        new TextComponentTranslation(get("skill.black_scouring_flame.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLACK_SCOURING_FLAME,
                                        false
                                ),
                                2, 0
                        );
                        SkillAdvancementConditionSimple black_blessing_flame = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_blessing_flame.title")),
                                        new TextComponentTranslation(get("skill.black_blessing_flame.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLACK_BLESSING_FLAME,
                                        false
                                ),
                                2, 2
                        );
                        SkillAdvancementConditionSimple black_raging_flame_ball = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_raging_flame_ball.title")),
                                        new TextComponentTranslation(get("skill.black_raging_flame_ball.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLACK_RAGING_FLAME_BALL,
                                        false
                                ),
                                4, 0
                        );
                        SkillAdvancementConditionSimple black_volley_flame = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_volley_flame.title")),
                                        new TextComponentTranslation(get("skill.black_volley_flame.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLACK_VOLLEY_FLAME,
                                        false
                                ),
                                4, 2
                        );
                        SkillAdvancementConditionSimple black_flare_flame = new SkillAdvancementConditionSimple(
                                new SkillAdvancementInfo(
                                        new TextComponentTranslation(get("skill.black_flare_flame.title")),
                                        new TextComponentTranslation(get("skill.black_flare_flame.description")),
                                        SkillAdvancementInfo.Frame.ROUNDED,
                                        ModAbilities.BLACK_FLARE_FLAME,
                                        false
                                ),
                                6, 1
                        );
                        //Requirements
                        black_scouring_flame.addCondition(black_flame_ball);
                        black_scouring_flame.addCondition(new SkillAdvancementConditionNotOrOverride(black_blessing_flame, black_flare_flame));
                        black_blessing_flame.addCondition(black_flame_ball);
                        black_blessing_flame.addCondition(new SkillAdvancementConditionNotOrOverride(black_scouring_flame, black_flare_flame));
                        black_raging_flame_ball.addCondition(new SkillAdvancementConditionOr(black_scouring_flame, black_blessing_flame));
                        black_raging_flame_ball.addCondition(new SkillAdvancementConditionNotOrOverride(black_volley_flame, black_flare_flame));
                        black_raging_flame_ball.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(black_raging_flame_ball, black_flare_flame, black_scouring_flame, black_blessing_flame));
                        black_volley_flame.addCondition(new SkillAdvancementConditionOr(black_scouring_flame, black_blessing_flame));
                        black_volley_flame.addCondition(new SkillAdvancementConditionNotOrOverride(black_raging_flame_ball, black_flare_flame));
                        black_volley_flame.addCondition(new SkillAdvancementConditionWhenOverrideOrUpgraded(black_volley_flame, black_flare_flame, black_scouring_flame, black_blessing_flame));
                        black_flare_flame.addCondition(new SkillAdvancementConditionOr(black_volley_flame, black_raging_flame_ball));

                        damage.addCondition(black_flame_ball);
                        attack_speed.addCondition(black_flame_ball);
                        knockback.addCondition(black_flame_ball);
                        ability_power.addCondition(new SkillAdvancementConditionOr(black_flame_ball, black_flame_ball));
                        critical_chance.addCondition(new SkillAdvancementConditionOr(black_flame_ball, black_flame_ball));
                        armor_penetration.addCondition(new SkillAdvancementConditionOr(black_flame_ball, black_flame_ball));
                        //Altar Requirements
                        ability_power.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        critical_chance.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        armor_penetration.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_1));
                        black_flame_ball.addCondition(new SkillAdvancementConditionAltar(SkillAdvancementConditionAltar.LEVEL_0));
                        //GUI
                        GuiSkillAdvancement gui0 = black_flame.addAdvancement(black_flame_ball);
                        GuiSkillAdvancement gui1 = black_flame.addAdvancement(black_scouring_flame, black_blessing_flame);
                        GuiSkillAdvancement gui2 = black_flame.addAdvancement(black_raging_flame_ball, black_volley_flame);
                        GuiSkillAdvancement gui3 = black_flame.addAdvancement(black_flare_flame);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        black_flame.addAdvancement(damage, attack_speed, knockback);
                        black_flame.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }

                    if (CommonConfig.getSyncValues().advancement.oneTreePerClass) {
                        shadow.addCondition(new SkillAdvancementConditionNot(bleed));
                        shadow.addCondition(new SkillAdvancementConditionNot(slash));
                        shadow.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        shadow.addCondition(new SkillAdvancementConditionNot(radiant_ray));
                        shadow.addCondition(new SkillAdvancementConditionNot(black_flame_ball));
                        bleed.addCondition(new SkillAdvancementConditionNot(shadow));
                        bleed.addCondition(new SkillAdvancementConditionNot(slash));
                        bleed.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        bleed.addCondition(new SkillAdvancementConditionNot(radiant_ray));
                        bleed.addCondition(new SkillAdvancementConditionNot(black_flame_ball));
                        slash.addCondition(new SkillAdvancementConditionNot(shadow));
                        slash.addCondition(new SkillAdvancementConditionNot(bleed));
                        slash.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        slash.addCondition(new SkillAdvancementConditionNot(radiant_ray));
                        slash.addCondition(new SkillAdvancementConditionNot(black_flame_ball));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(shadow));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(bleed));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(slash));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(radiant_ray));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(black_flame_ball));
                        radiant_ray.addCondition(new SkillAdvancementConditionNot(shadow));
                        radiant_ray.addCondition(new SkillAdvancementConditionNot(bleed));
                        radiant_ray.addCondition(new SkillAdvancementConditionNot(slash));
                        radiant_ray.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        radiant_ray.addCondition(new SkillAdvancementConditionNot(black_flame_ball));
                        black_flame_ball.addCondition(new SkillAdvancementConditionNot(shadow));
                        black_flame_ball.addCondition(new SkillAdvancementConditionNot(bleed));
                        black_flame_ball.addCondition(new SkillAdvancementConditionNot(slash));
                        black_flame_ball.addCondition(new SkillAdvancementConditionNot(radiant_ray));
                        black_flame_ball.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                    }
                    return window;
                }
                return null;
            case LibGui.PAUSE_ALL:
                return new GuiPauseAll();
            default:
                return null;
        }
    }

    public String get(String key) {
        return "ui." + LibMod.MOD_ID + "." + key;
    }
}
