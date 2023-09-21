package arekkuusu.enderskills.common.skill.attribute.mobility;

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

public class SwimSpeed extends BaseAttribute {

    public static final DynamicModifier SPEED_ATTRIBUTE = new DynamicModifier(
            "d52dc4d0-db1d-4090-83ea-322d1777d48b",
            LibMod.MOD_ID + ":" + LibNames.SWIM_SPEED,
            SharedMonsterAttributes.MOVEMENT_SPEED,
            Constants.AttributeModifierOperation.ADD_MULTIPLE);

    public SwimSpeed() {
        super(LibNames.SWIM_SPEED, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this) && entity.isWet()) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    SPEED_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(ModAttributes.SWIM_SPEED, attributeInfo.getLevel()));
                });
            } else {
                SPEED_ATTRIBUTE.remove(entity);
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_MOBILITY_FOLDER + LibNames.SWIM_SPEED;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
