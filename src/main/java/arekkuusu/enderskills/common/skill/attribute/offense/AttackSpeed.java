package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.mobility.FallResistance;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AttackSpeed extends BaseAttribute implements ISkillAdvancement {

    public static final UUID ITEM_ATTACK_SPEED_MODIFIER;

    static {
        ITEM_ATTACK_SPEED_MODIFIER = ObfuscationReflectionHelper.getPrivateValue(
                Item.class, null,
                "ATTACK_SPEED_MODIFIER", "field_185050_h"
        );
    }

    public static final DynamicModifier SPEED_ATTRIBUTE = new DynamicModifier(
            "2b3b2ec9-00d5-43e7-86f4-bb51d6c5c1e7",
            LibMod.MOD_ID + ":" + LibNames.ATTACK_SPEED,
            SharedMonsterAttributes.ATTACK_SPEED,
            Constants.AttributeModifierOperation.ADD);

    public AttackSpeed() {
        super(LibNames.ATTACK_SPEED, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 40 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    ItemStack heldMain = entity.getHeldItemMainhand();
                    ItemStack heldOff = entity.getHeldItemOffhand();
                    double heldMainSpeed = getItemStackSpeedModifier(entity, heldMain);
                    double heldOffSpeed = getItemStackSpeedModifier(entity, heldOff);
                    double bigChungus = Math.min(heldMainSpeed, heldOffSpeed);
                    SPEED_ATTRIBUTE.apply(entity, getModifier(attributeInfo) * getChungness(bigChungus));
                });
            } else {
                SPEED_ATTRIBUTE.remove(entity);
            }
        });
    }

    public double getChungness(double chungusThicc) {
        return MathHelper.clamp(chungusThicc / SharedMonsterAttributes.ATTACK_SPEED.getDefaultValue(), 0F, 1F);
    }

    public double getItemStackSpeedModifier(EntityLivingBase entity, ItemStack stack) {
        double amountAttribute = 0;
        Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entries()) {
            AttributeModifier attributemodifier = entry.getValue();
            if (attributemodifier.getID() == ITEM_ATTACK_SPEED_MODIFIER) {
                amountAttribute = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                break;
            }
        }
        return amountAttribute;
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
                        description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        if (attributeInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", attributeInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.boost", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
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
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ATTACK_SPEED;
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
                    "├ max_level: 20",
                    "└ ",
                    "",
                    "┌ MODIFIER (",
                    "│     shape: flat",
                    "│     min: 0%",
                    "│     max: 80%",
                    "│ ",
                    "│     {0} [",
                    "│         shape: multiply 4%",
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
                    "│         shape: solve for 5 + 35 * {level}",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
