package arekkuusu.enderskills.common.skill.attribute.deffense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HeartBoost extends BaseAttribute {

    public static final DynamicModifier HEALTH_ATTRIBUTE = new DynamicModifier(
            "b49606db-d189-4e63-a577-638b00c9d782",
            LibMod.MOD_ID + ":" + LibNames.HEART_BOOST,
            SharedMonsterAttributes.MAX_HEALTH,
            Constants.AttributeModifierOperation.ADD);

    public HeartBoost() {
        super(LibNames.HEART_BOOST, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
                    HEALTH_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(ModAttributes.HEART_BOOST, attributeInfo.getLevel()));
                });
            } else {
                HEALTH_ATTRIBUTE.remove(entity);
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_DEFENSE_FOLDER + LibNames.HEART_BOOST;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
