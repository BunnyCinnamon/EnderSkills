package arekkuusu.enderskills.common.skill.attribute;

import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ModAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class BaseAttribute extends Skill {

    public BaseAttribute(String id, Properties properties) {
        super(properties.setHasTexture());
        ModAttributes.setRegistry(this, id);
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AttributeInfo(compound);
    }
}
