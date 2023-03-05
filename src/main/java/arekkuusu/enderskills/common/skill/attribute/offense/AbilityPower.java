package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class AbilityPower extends BaseAttribute implements ISkillAdvancement {

    //Vanilla Attribute
    public static final IAttribute ABILITY_POWER = new RangedAttribute(null, "enderskills.generic.abilityPower", 0F, 0F, Float.MAX_VALUE).setDescription("Ability Power").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ABILITY_POWER_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84adc38600",
            LibMod.MOD_ID + ":" + LibNames.ABILITY_POWER,
            AbilityPower.ABILITY_POWER,
            Constants.AttributeModifierOperation.ADD);

    public AbilityPower() {
        super(LibNames.ABILITY_POWER, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
        ((BaseProperties) getProperties()).setTopLevelGetter(this::getTopLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    ABILITY_POWER_ATTRIBUTE.apply(entity, getModifier(attributeInfo));
                });
            } else {
                ABILITY_POWER_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || !SkillHelper.isSkillDamage(event.getSource())) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        double amount = entity.getEntityAttribute(AbilityPower.ABILITY_POWER).getAttributeValue();
        if (!MathUtil.fuzzyEqual(0, amount)) {
            if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                event.setAmount(event.getAmount() + event.getAmount() * amount);
            } else {
                event.setAmount(event.getAmount() + amount);
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase)
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(ABILITY_POWER).setBaseValue(0F);
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public int getTopLevel() {
        return this.config.top_level;
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
                        if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        } else {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(attributeInfo))));
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
                            if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                                description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            } else {
                                description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(infoNew))));
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

    @Override
    public int getEndurance(int lvl) {
        return (int) this.config.get(this, "ENDURANCE", lvl);
    }

    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ABILITY_POWER;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        Configuration.CONFIG_SYNC.applyAs = Configuration.CONFIG.applyAs;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
        initSyncConfig();
        NBTHelper.setEnum(compound, "applyAs", Configuration.CONFIG.applyAs);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
        sigmaDic();
        Configuration.CONFIG_SYNC.applyAs = NBTHelper.getEnum(Configuration.Damage.class, compound, "applyAs");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static Configuration.Values CONFIG = new Configuration.Values();

        public enum Damage implements IStringSerializable {
            MULTIPLICATION,
            ADDITION;

            @Override
            public String getName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public static class Values {

            public Configuration.Damage applyAs = Configuration.Damage.MULTIPLICATION;

            public String[] dsl = {
                    "",
                    "┌ v1.0",
                    "│ ",
                    "├ min_level: 0",
                    "├ top_level: 50",
                    "├ max_level: infinite",
                    "└ ",
                    "",
                    "┌ MODIFIER (",
                    "│     shape: flat",
                    "│     min: 0%",
                    "│     max: infinite",
                    "│ ",
                    "│     {0 to 50} [",
                    "│         shape: curve positive 3.25",
                    "│         start: {min}",
                    "│         end: 100%",
                    "│     ]",
                    "│ ",
                    "│     {51} [",
                    "│         shape: solve for 1 + ({level} * 0.1)",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 0",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: 69",
                    "│     ]",
                    "│ ",
                    "│     {1} [",
                    "│         shape: solve for 5 + 14 * {level}",
                    "│     ]",
                    "│ ",
                    "│     {51} [",
                    "│         shape: none",
                    "│         start: " + XPHelper.getXPValueFromLevel(30),
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
