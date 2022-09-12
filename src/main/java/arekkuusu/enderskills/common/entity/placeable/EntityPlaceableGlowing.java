package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableGlowing extends EntityPlaceableData {

    public EntityPlaceableGlowing(World world) {
        super(world);
    }

    public EntityPlaceableGlowing(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }
}
