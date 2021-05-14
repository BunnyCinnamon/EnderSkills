package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLivingBase;

public class PotionEnduranceRegen extends PotionBase {

    protected PotionEnduranceRegen() {
        super(LibNames.ENDURANCE_REGEN_EFFECT, 0x7200FA, 2);
    }

    @Override
    public void onApply(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote) return;
        Capabilities.endurance(entity).ifPresent(capability -> {
            capability.setEnduranceDelay(capability.getEnduranceDelay() - 1);
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
