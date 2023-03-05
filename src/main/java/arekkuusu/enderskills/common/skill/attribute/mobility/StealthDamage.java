package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import arekkuusu.enderskills.common.skill.attribute.deffense.DamageResistance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class StealthDamage extends BaseAttribute implements ISkillAdvancement {

    public StealthDamage() {
        super(LibNames.STEALTH_DAMAGE, new BaseProperties());
        MinecraftForge.EVENT_BUS.register(this);
        ((BaseProperties) getProperties()).setMaxLevelGetter(this::getMaxLevel);
        ((BaseProperties) getProperties()).setTopLevelGetter(this::getTopLevel);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.getSource().getDamageType().equals(BaseAbility.DAMAGE_HIT_TYPE)) return;
        DamageSource source = event.getSource();
        if (!source.getDamageType().matches("player|mob")) return;
        if (!(source.getTrueSource() instanceof EntityLivingBase) || source instanceof SkillDamageSource || source.getImmediateSource() != source.getTrueSource())
            return;
        EntityLivingBase target = event.getEntityLiving();
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Stealth
            if (capability.isOwned(this) && attacker.isSneaking() && isNotSeenByTarget(attacker, target)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    event.setAmount(event.getAmount() + (event.getAmount() * getModifier(attributeInfo)));
                });
            }
        });
    }

    public boolean isNotSeenByTarget(EntityLivingBase attacker, EntityLivingBase target) {
        Vec3d positionTarget = target.getPositionEyes(1F);
        Vec3d lookTarget = target.getLookVec().normalize();
        Vec3d positionAttacker = attacker.getPositionEyes(1F);

        Vec3d origin = new Vec3d(0, 0, 0);
        Vec3d pointA = lookTarget.add(positionTarget).subtract(positionTarget);
        Vec3d pointB = positionAttacker.subtract(positionTarget);
        double pointADistance = pointA.distanceTo(pointB);
        double pointBDistance = pointB.distanceTo(origin);

        if (pointBDistance <= 5 && pointADistance > pointBDistance) {
            double ab = (pointA.x * pointB.x) + (pointA.y * pointB.y) + (pointA.z * pointB.z);
            double a = Math.sqrt(Math.pow(pointA.x, 2D) + Math.pow(pointA.y, 2D) + Math.pow(pointA.z, 2D));
            double b = Math.sqrt(Math.pow(pointB.x, 2D) + Math.pow(pointB.y, 2D) + Math.pow(pointB.z, 2D));
            double angle = Math.acos(ab / (a * b)) * (180 / Math.PI);
            return angle < 280 && angle > 80;
        }
        return false;
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
                        description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        if (attributeInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", attributeInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.dmg", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
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
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_MOBILITY_FOLDER + LibNames.STEALTH_DAMAGE;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
        initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
        sigmaDic();
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

        public static class Values {

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
