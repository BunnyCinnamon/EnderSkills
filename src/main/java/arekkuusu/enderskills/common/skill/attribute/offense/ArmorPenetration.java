package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArmorPenetration extends BaseAttribute {

    public ArmorPenetration() {
        super(LibNames.ARMOR_PENETRATION, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || source instanceof SkillDamageSource) return;
        EntityLivingBase target = event.getEntityLiving();
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Penetration
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    int armor = target.getTotalArmorValue();
                    if (armor > 0) {
                        double toughness = target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue();
                        float damage = CombatRules.getDamageAfterAbsorb(event.getAmount(), armor, (float) toughness);
                        event.setAmount(event.getAmount() + ((event.getAmount() - damage) * DSLDefaults.getModifier(ModAttributes.ARMOR_PENETRATION, attributeInfo.getLevel())));
                    }
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ARMOR_PENETRATION;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
