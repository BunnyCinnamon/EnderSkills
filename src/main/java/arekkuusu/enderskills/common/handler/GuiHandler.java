package arekkuusu.enderskills.common.handler;

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
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case LibGui.LEVEL_EDITING:
                return null;
            case LibGui.PAUSE_ALL:
                return null;
            default:
                return null;
        }
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

                GuiScreenSkillAdvancements window = new GuiScreenSkillAdvancements();
                GuiSkillAdvancementTab tab0 = window.addTab(new TextComponentTranslation(get("tab.defense.title")), SkillAdvancementTabType.BELOW, 0x65974B, 0);
                if (tab0 != null) {
                    GuiSkillAdvancementPage page0 = tab0.addPage(new TextComponentTranslation(get("page.light.title")));
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
                        GuiSkillAdvancement gui0 = page0.addAdvancement(charm);
                        GuiSkillAdvancement gui1 = page0.addAdvancement(heal_aura, power_boost);
                        GuiSkillAdvancement gui2 = page0.addAdvancement(heal_other, heal_self);
                        GuiSkillAdvancement gui3 = page0.addAdvancement(nearby_invisibility);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page0.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        page0.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }
                    GuiSkillAdvancementPage page1 = tab0.addPage(new TextComponentTranslation(get("page.earth.title")));
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
                        GuiSkillAdvancement gui0 = page1.addAdvancement(taunt);
                        GuiSkillAdvancement gui1 = page1.addAdvancement(wall, dome);
                        GuiSkillAdvancement gui2 = page1.addAdvancement(thorny, shockwave);
                        GuiSkillAdvancement gui3 = page1.addAdvancement(animatedStoneGolem);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page1.addAdvancement(explosion_resistance, damage_resistance, knockback_resistance);
                        page1.addAdvancement(magic_resistance, heart_boost, fire_resistance);
                    }

                    if (CommonConfig.getValues().advancement.oneTreePerClass) {
                        charm.addCondition(new SkillAdvancementConditionNot(taunt));
                        taunt.addCondition(new SkillAdvancementConditionNot(charm));
                    }
                }
                GuiSkillAdvancementTab tab1 = window.addTab(new TextComponentTranslation(get("tab.mobility.title")), SkillAdvancementTabType.BELOW, 0x329CA2, 1);
                if (tab1 != null) {
                    GuiSkillAdvancementPage page0 = tab1.addPage(new TextComponentTranslation(get("page.wind.title")));
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
                        GuiSkillAdvancement gui0 = page0.addAdvancement(dash);
                        GuiSkillAdvancement gui1 = page0.addAdvancement(extra_jump, fog);
                        GuiSkillAdvancement gui2 = page0.addAdvancement(smash, hasten);
                        GuiSkillAdvancement gui3 = page0.addAdvancement(speedBoost);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page0.addAdvancement(jump_height, speed, fall_resistance);
                        page0.addAdvancement(endurance, stealth_damage, swim_speed);
                    }
                    GuiSkillAdvancementPage page1 = tab1.addPage(new TextComponentTranslation(get("page.void.title")));
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
                        GuiSkillAdvancement gui0 = page1.addAdvancement(warp);
                        GuiSkillAdvancement gui1 = page1.addAdvancement(invisibility, hover);
                        GuiSkillAdvancement gui2 = page1.addAdvancement(unstable_portal, portal);
                        GuiSkillAdvancement gui3 = page1.addAdvancement(teleport);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page1.addAdvancement(jump_height, speed, fall_resistance);
                        page1.addAdvancement(endurance, stealth_damage, swim_speed);
                    }

                    if (CommonConfig.getValues().advancement.oneTreePerClass) {
                        dash.addCondition(new SkillAdvancementConditionNot(warp));
                        warp.addCondition(new SkillAdvancementConditionNot(dash));
                    }
                }
                GuiSkillAdvancementTab tab2 = window.addTab(new TextComponentTranslation(get("tab.offense.title")), SkillAdvancementTabType.BELOW, 0xAF3B3E, 2);
                if (tab2 != null) {
                    GuiSkillAdvancementPage page0 = tab2.addPage(new TextComponentTranslation(get("page.void.title")));
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
                        GuiSkillAdvancement gui0 = page0.addAdvancement(shadow);
                        GuiSkillAdvancement gui1 = page0.addAdvancement(gloom, shadow_jab);
                        GuiSkillAdvancement gui2 = page0.addAdvancement(gas_cloud, grasp);
                        GuiSkillAdvancement gui3 = page0.addAdvancement(black_hole);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page0.addAdvancement(damage, attack_speed, knockback);
                        page0.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage page1 = tab2.addPage(new TextComponentTranslation(get("page.blood.title")));
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
                        GuiSkillAdvancement gui0 = page1.addAdvancement(bleed);
                        GuiSkillAdvancement gui1 = page1.addAdvancement(blood_pool, contaminate);
                        GuiSkillAdvancement gui2 = page1.addAdvancement(life_steal, syphon);
                        GuiSkillAdvancement gui3 = page1.addAdvancement(sacrifice);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page1.addAdvancement(damage, attack_speed, knockback);
                        page1.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage page2 = tab2.addPage(new TextComponentTranslation(get("page.wind.title")));
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
                        GuiSkillAdvancement gui0 = page2.addAdvancement(slash);
                        GuiSkillAdvancement gui1 = page2.addAdvancement(push, pull);
                        GuiSkillAdvancement gui2 = page2.addAdvancement(crush, updraft);
                        GuiSkillAdvancement gui3 = page2.addAdvancement(suffocate);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page2.addAdvancement(damage, attack_speed, knockback);
                        page2.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }
                    GuiSkillAdvancementPage page3 = tab2.addPage(new TextComponentTranslation(get("page.fire.title")));
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
                        GuiSkillAdvancement gui0 = page3.addAdvancement(fire_spirit);
                        GuiSkillAdvancement gui1 = page3.addAdvancement(flaming_breath, flaming_rain);
                        GuiSkillAdvancement gui2 = page3.addAdvancement(focus_flame, fireball);
                        GuiSkillAdvancement gui3 = page3.addAdvancement(explode);
                        gui0.addChildren(gui1);
                        gui1.addChildren(gui2);
                        gui2.addChildren(gui3);
                        page3.addAdvancement(damage, attack_speed, knockback);
                        page3.addAdvancement(ability_power, critical_chance, armor_penetration);
                    }

                    if (CommonConfig.getValues().advancement.oneTreePerClass) {
                        shadow.addCondition(new SkillAdvancementConditionNot(bleed));
                        shadow.addCondition(new SkillAdvancementConditionNot(slash));
                        shadow.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        bleed.addCondition(new SkillAdvancementConditionNot(shadow));
                        bleed.addCondition(new SkillAdvancementConditionNot(slash));
                        bleed.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        slash.addCondition(new SkillAdvancementConditionNot(shadow));
                        slash.addCondition(new SkillAdvancementConditionNot(bleed));
                        slash.addCondition(new SkillAdvancementConditionNot(fire_spirit));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(shadow));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(bleed));
                        fire_spirit.addCondition(new SkillAdvancementConditionNot(slash));
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
