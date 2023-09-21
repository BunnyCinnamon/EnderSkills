package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CriticalChance extends BaseAttribute {

    public CriticalChance() {
        super(LibNames.CRITICAL_CHANCE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
                        if (attacker.world.rand.nextDouble() < DSLDefaults.getModifier(ModAttributes.CRITICAL_CHANCE, attributeInfo.getLevel())) {
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
                    if (attacker.world.rand.nextDouble() < DSLDefaults.getModifier(ModAttributes.CRITICAL_CHANCE, attributeInfo.getLevel())) {
                        event.setDamageModifier(1.5F);
                        event.setResult(Event.Result.ALLOW);
                    }
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.CRITICAL_CHANCE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
