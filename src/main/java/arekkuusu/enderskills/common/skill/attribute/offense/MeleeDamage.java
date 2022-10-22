package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.defense.fire.Overheat;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class MeleeDamage extends BaseAttribute implements ISkillAdvancement {

    public MeleeDamage() {
        super(LibNames.DAMAGE, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || source instanceof SkillDamageSource || event.getAmount() <= 0)
            return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Damage
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage_.MULTIPLICATION) {
                        event.setAmount(event.getAmount() + event.getAmount() * getModifier(attributeInfo));
                    } else {
                        event.setAmount(event.getAmount() + getModifier(attributeInfo));
                    }
                });
            }
        });
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public float getModifier(AttributeInfo info) {
        return (float) this.config.get(this, "MODIFIER", info.getLevel());
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
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        description.clear();
                        if (attributeInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max"));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", attributeInfo.getLevel(), attributeInfo.getLevel() + 1));
                        }
                        if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage_.MULTIPLICATION) {
                            description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        } else {
                            description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(attributeInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                        }
                        if (attributeInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", attributeInfo.getLevel(), infoNew.getLevel()));
                            if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage_.MULTIPLICATION) {
                                description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            } else {
                                description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_hearts")));
                            }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.DAMAGE;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
        Configuration.CONFIG_SYNC.applyAs = Configuration.CONFIG.applyAs;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
        NBTHelper.setEnum(compound, "applyAs", Configuration.CONFIG.applyAs);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
        Configuration.CONFIG_SYNC.applyAs = NBTHelper.getEnum(Configuration.Damage_.class, compound, "applyAs");
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

        public enum Damage_ implements IStringSerializable {
            MULTIPLICATION,
            ADDITION;

            @Override
            public String getName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public static class Values {

            public Configuration.Damage_ applyAs = Configuration.Damage_.MULTIPLICATION;

            public String[] dsl = {
                    "",
                    "┌ v1.0",
                    "│ ",
                    "├ min_level: 0",
                    "├ max_level: infinite",
                    "└ ",
                    "",
                    "┌ MODIFIER (",
                    "│     shape: flat",
                    "│     min: 0%",
                    "│     max: 99%",
                    "│ ",
                    "│     {0} [",
                    "│         shape: solve for 1 - e^(-0.05 * {level})",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 69",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: {min}",
                    "│     ]",
                    "│ ",
                    "│     {1} [",
                    "│         shape: solve for 5 + 14 * {level}",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
