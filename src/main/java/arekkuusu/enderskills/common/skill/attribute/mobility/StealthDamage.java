package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.IInfoUpgradeable;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
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
        super(LibNames.STEALTH_DAMAGE);
        MinecraftForge.EVENT_BUS.register(this);
        setTexture(ResourceLibrary.ATTRIBUTE_1_1);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.getSource().getDamageType().equals("ability")) return;
        DamageSource source = event.getSource();
        if (!source.getDamageType().matches("player|mob")) return;
        if (source.getTrueSource() == null || source.getImmediateSource() != source.getTrueSource()) return;
        EntityLivingBase target = event.getEntityLiving();
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Stealth
            if (capability.owns(this) && attacker.isSneaking() && isNotSeenByTarget(attacker, target)) {
                capability.get(this).ifPresent(skillInfo -> {
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
        //Vec3d lookAttacker = attacker.getLookVec().normalize();

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

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    @Override
    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public float getModifier(AttributeInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().modifier, level, levelMax);
        return (float) (func * getEffectiveness());
    }

    public double getEffectiveness() {
        return Configuration.getSyncValues().effectiveness * CommonConfig.getSyncValues().skill.globalEffectiveness;
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.owns(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add("Hold SHIFT for stats.");
                } else {
                    c.get(this).ifPresent(skillInfo -> {
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        description.clear();
                        if (attributeInfo.getLevel() >= getMaxLevel()) {
                            description.add("Max Level:");
                        } else {
                            description.add("Current Level:");
                        }
                        description.add("Dmg: +" + TextHelper.format2FloatPoint(getModifier(attributeInfo) * 100) + " %");
                        if (attributeInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AttributeInfo infoNew = new AttributeInfo(attributeInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Dmg: +" + TextHelper.format2FloatPoint(getModifier(infoNew) * 100) + " %");
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean canUpgrade(EntityLivingBase entity) {
        return Capabilities.advancement(entity).map(c -> {
            Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            return c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            if (c.getExperienceTotal(entity) >= xp) {
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public Requirement getRequirement(EntityLivingBase entity) {
        AttributeInfo info = (AttributeInfo) Capabilities.get(entity).flatMap(a -> a.get(this)).orElse(null);
        return new DefaultRequirement(0, getUpgradeCost(info));
    }

    public int getUpgradeCost(@Nullable AttributeInfo info) {
        int level = info != null ? getLevel(info) + 1 : 0;
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().advancement.upgrade, level, levelMax);
        return (int) (func * CommonConfig.getSyncValues().advancement.globalCostMultiplier);
    }
    /*Advancement Section*/

    @Override
    public void initSyncConfig() {
        Configuration.getSyncValues().maxLevel = Configuration.getValues().maxLevel;
        Configuration.getSyncValues().modifier = Configuration.getValues().modifier;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        DamageResistance.Configuration.getSyncValues().advancement.upgrade = DamageResistance.Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        compound.setString("modifier", Configuration.getValues().modifier);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        compound.setString("advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().modifier = compound.getString("modifier");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = compound.getString("advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Attribute/" + LibNames.STEALTH_DAMAGE)
    public static class Configuration {

        @Config.Comment("Attribute Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.STEALTH_DAMAGE)
        public static Values CONFIG = new Values();

        public static Values getValues() {
            return CONFIG;
        }

        @Config.Ignore
        protected static Values CONFIG_SYNC = new Values();

        public static Values getSyncValues() {
            return CONFIG_SYNC;
        }

        public static class Values {
            @Config.Comment("Skill specific Advancement Configuration")
            public final Advancement advancement = new Advancement();

            @Config.Comment("The Maximum level of this Skill")
            @Config.RangeInt(min = 0)
            public int maxLevel = Integer.MAX_VALUE;

            @Config.Comment("Modifier Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String modifier = "1 - e^(-0.05 * x)";

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String upgrade = "(690 * (1 - (0 ^ (0 ^ x)))) + 7 * x";
            }
        }
    }
}
