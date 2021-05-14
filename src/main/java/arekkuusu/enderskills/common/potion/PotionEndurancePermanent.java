package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionEndurancePermanent extends PotionBase {

    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ENDURANCE_ATTRIBUTE = new DynamicModifier(
            "010bf31b-310d-4ef9-91ed-6f84adc38600",
            LibMod.MOD_ID + ":" + LibNames.ENDURANCE_PERMANENT_EFFECT,
            Endurance.MAX_ENDURANCE,
            Constants.AttributeModifierOperation.ADD_MULTIPLE);

    protected PotionEndurancePermanent() {
        super(LibNames.ENDURANCE_PERMANENT_EFFECT, 0x7200AF, 2);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() == null) return;
        if (event.getEntityLiving().world.isRemote) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (entity.isPotionActive(ModPotions.POTION_ABILITY_POWER_EFFECT)) {
                PotionEffect effect = entity.getActivePotionEffect(ModPotions.POTION_ABILITY_POWER_EFFECT);
                assert effect != null; //Shut up
                ENDURANCE_ATTRIBUTE.apply(entity, 0.1 * (effect.getAmplifier() + 1));
            } else {
                if (ENDURANCE_ATTRIBUTE.remove(entity)) {
                    Capabilities.endurance(entity).ifPresent(enduranceCapability -> {
                        double amount = entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
                        if (enduranceCapability.getEndurance() > amount) {
                            enduranceCapability.setEndurance(amount);
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int k = 50 >> amplifier;

        if (k > 0) {
            return duration % k == 0;
        } else {
            return true;
        }
    }
}
