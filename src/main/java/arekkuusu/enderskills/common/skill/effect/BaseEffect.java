package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ModAttributes;
import net.minecraft.entity.EntityLivingBase;

public class BaseEffect extends Skill {

    public static final int INDEFINITE = -1;
    public static final int INSTANT = 0;

    public BaseEffect(String id, Properties properties) {
        super(properties);
        ModAttributes.setRegistry(this, id);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        this.update(entity, data, 0);
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    public void set(EntityLivingBase entity, SkillData data) {
        //For-Rent
    }
}
