package arekkuusu.enderskills.api.capability.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public interface InfoCooldown {

    String COOL_DOWN = "cooldown";

    void setCooldown(int cooldown);

    int getCooldown();

    boolean hasCooldown();

    default boolean canSetCooldown(EntityLivingBase owner) {
        return !(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode;
    }
}
