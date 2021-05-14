package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableBloodPool extends EntityPlaceableFloor {

    public EntityPlaceableBloodPool(World world) {
        super(world);
    }

    public EntityPlaceableBloodPool(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Nullable
    public BlockPos getValid(BlockPos pos) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos);

        if (!isSolid(mPos)) {
            for (int j = 0; ; j++) {
                if (j == 1) return null;
                mPos.move(EnumFacing.DOWN);
                if (isSolid(mPos)) {
                    return mPos.toImmutable();
                }
            }
        } else if (isSolid(mPos.up())) {
            return null;
        }
        return mPos.toImmutable();
    }
}
