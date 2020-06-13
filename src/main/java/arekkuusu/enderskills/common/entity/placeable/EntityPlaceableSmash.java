package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableSmash extends EntityPlaceableData {

    public EntityPlaceableSmash(World world) {
        super(world);
        setSize(0F, 1F);
    }

    public EntityPlaceableSmash(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
        setSize(0F, 1F);
    }
}
