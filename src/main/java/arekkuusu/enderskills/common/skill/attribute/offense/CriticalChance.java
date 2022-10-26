package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.mobility.FallResistance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class CriticalChance extends BaseAttribute implements ISkillAdvancement {

    public CriticalChance() {
        super(LibNames.CRITICAL_CHANCE, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof EntityLivingBase) || !(event.getSource().getTrueSource() instanceof EntityLivingBase) || isClientWorld(event.getEntityLiving()))
            return;
        DamageSource source = event.getSource();
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        if (attacker != null && source.getDamageType().equals("mob")) {
            Capabilities.get(attacker).ifPresent(capability -> {
                //Do Critical
                if (capability.isOwned(this)) {
                    capability.getOwned(this).ifPresent(skillInfo -> {
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        if (attacker.world.rand.nextDouble() < getModifier(attributeInfo)) {
                            event.setAmount(event.getAmount() * 1.5F);
                        }
                    });
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityCritical(CriticalHitEvent event) {
        if (event.getDamageModifier() > 1F || !(event.getEntity() instanceof EntityLivingBase) || isClientWorld(event.getEntityLiving()))
            return;
        EntityLivingBase attacker = event.getEntityLiving();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Critical
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    if (attacker.world.rand.nextDouble() < getModifier(attributeInfo)) {
                        event.setDamageModifier(1.5F);
                        event.setResult(Event.Result.ALLOW);
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
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.CRITICAL_CHANCE;
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
                    "│     min: 0",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: 69",
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
