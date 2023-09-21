package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StealthDamage extends BaseAttribute {

    public StealthDamage() {
        super(LibNames.STEALTH_DAMAGE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
                    event.setAmount(event.getAmount() + (event.getAmount() * DSLDefaults.getModifier(ModAttributes.STEALTH_DAMAGE, attributeInfo.getLevel())));
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

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_MOBILITY_FOLDER + LibNames.STEALTH_DAMAGE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
