package arekkuusu.enderskills.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

public class AIFollowFlyingProvider extends AIFollowProvider {

    public AIFollowFlyingProvider(EntityLiving entity, Supplier<EntityLivingBase> ownerSupplier, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(entity, ownerSupplier, followSpeedIn, minDistIn, maxDistIn);
    }

    protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_) {
        return this.world.isAirBlock(new BlockPos(x + p_192381_4_, y, p_192381_2_ + p_192381_5_));
    }
}
