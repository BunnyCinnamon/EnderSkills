package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class PotionEndurance extends PotionBase {

    protected PotionEndurance() {
        super(LibNames.ENDURANCE_EFFECT, 0x7200FF, 2);
    }

    @Override
    public void onApply(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote) return;
        Capabilities.endurance(entity).ifPresent(capability -> {
            double maxEndurance = entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
            double endurance = capability.getEndurance();
            double enduranceExtra = (int) (maxEndurance * 0.5 * (amplifier + 1));
            double enduranceTotal = endurance + enduranceExtra;
            if (enduranceTotal > maxEndurance) enduranceTotal = maxEndurance;
            capability.setEndurance(enduranceTotal);
            if (entity instanceof EntityPlayerMP) {
                PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
            }
        });
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean isInstant() {
        return true;
    }
}
