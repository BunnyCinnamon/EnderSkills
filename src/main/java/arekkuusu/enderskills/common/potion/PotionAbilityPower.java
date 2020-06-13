package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionAbilityPower extends PotionBase {

    protected PotionAbilityPower() {
        super(LibNames.ABILITY_POWER_EFFECT, 0xFF0000, 0);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (event.getEntityLiving().world.isRemote || !event.getSource().getDamageType().equals("skill")) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.isPotionActive(ModPotions.POTION_ABILITY_POWER_EFFECT)) {
            PotionEffect effect = entity.getActivePotionEffect(ModPotions.POTION_ABILITY_POWER_EFFECT);
            assert effect != null; //Shut up
            int amplifier = effect.getAmplifier() + 1;
            double value = 0.5D * amplifier * 2;
            if (amplifier >= 2) value += 0.5D;
            event.setAmount(event.getAmount() + (event.getAmount() * value));
        }
    }
}
