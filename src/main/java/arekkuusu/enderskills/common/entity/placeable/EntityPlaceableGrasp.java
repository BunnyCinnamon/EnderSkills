package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableGrasp extends EntityPlaceableFloor {

    public EntityPlaceableGrasp(World world) {
        super(world);
    }

    public EntityPlaceableGrasp(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }
}
