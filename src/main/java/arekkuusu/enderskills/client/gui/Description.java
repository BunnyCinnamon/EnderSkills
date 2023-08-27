package arekkuusu.enderskills.client.gui;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Pair;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.skill.ModAttributes;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class Description {

    public final List<Pair<String, Type>> list = Lists.newArrayList();
    public final String pre = "desc.stats.";
    public final Skill skill;

    public Description(Skill skill) {
        this.skill = skill;
    }

    public void add(String name, Type type) {
        this.list.add(new Pair<>(name, type));
    }

    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(skilledEntityCapability -> {
            if (skilledEntityCapability.isOwned(this.skill)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add(TextHelper.translate("desc.stats.shift"));
                } else {
                    skilledEntityCapability.getOwned(this.skill).ifPresent(skillInfo -> {
                        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                        int level = infoUpgradeable.getLevel();
                        int nextLevel = infoUpgradeable.getLevel() + 1;
                        int maxLevel = getMaxLevel();

                        description.clear();
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(getEndurance(level))));
                        description.add("");

                        if (level >= maxLevel) {
                            description.add(TextHelper.translate("desc.stats.level_max", maxLevel));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", level, nextLevel));
                        }

                        extracted(description, level);

                        if (level < maxLevel) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.level_next", level, nextLevel));

                                extracted(description, nextLevel);
                            }
                        }
                    });
                }
            }
        });
    }

    private void extracted(List<String> description, int level) {
        for (Pair<String, Type> pair : list) {
            String name = pair.l;
            Type type = pair.r;

            String suffix = pre;
            double value = DSLEvaluator.evaluateDouble(this.skill, name, level, 1D);
            if (type == Type.TIME) {
                value = value / 20D;
                suffix += "suffix_time";
            }
            if (type == Type.DAMAGE) {
                value = value / 2D;
                suffix += "suffix_hearts";
            }
            if (type == Type.HEALTH) {
                value = value / 2D;
                suffix += "suffix_hearts";
            }
            if (type == Type.PERCENTAGE) {
                value = value * 100D;
                suffix += "suffix_percentage";
            }
            description.add(TextHelper.translate(pre + name.toLowerCase(Locale.ROOT), TextHelper.format2FloatPoint(value), TextHelper.getTextComponent(suffix)));
        }
    }

    public int getMaxLevel() {
        return DSLEvaluator.evaluateMaxLevel(this.skill);
    }

    public int getEndurance(int level) {
        return ModAttributes.ENDURANCE.getEnduranceDrain(this.skill, level);
    }

    public enum Type {
        TIME,
        HEALTH,
        DAMAGE,
        PERCENTAGE
    }
}
