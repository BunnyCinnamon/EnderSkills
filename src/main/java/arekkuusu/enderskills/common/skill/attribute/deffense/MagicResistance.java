package arekkuusu.enderskills.common.skill.attribute.deffense;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MagicResistance extends BaseAttribute {

    public MagicResistance() {
        super(LibNames.MAGIC_RESISTANCE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMagicDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (event.getSource().isMagicDamage()) {
            EntityLivingBase entity = event.getEntityLiving();
            Capabilities.get(entity).ifPresent(capability -> {
                if (capability.isOwned(this)) {
                    capability.getOwned(this).ifPresent(skillInfo -> {
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        float damage = event.getAmount();
                        float resistance = 1 - DSLDefaults.getModifier(ModAttributes.MAGIC_RESISTANCE, attributeInfo.getLevel());
                        float reduction = damage * resistance;
                        event.setAmount(reduction);
                    });
                }
            });
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_DEFENSE_FOLDER + LibNames.MAGIC_RESISTANCE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
