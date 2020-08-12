package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLivingBase;

public class Overcharge extends BaseEffect {

    public Overcharge() {
        super(LibNames.OVERCHARGE, new Properties());
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
    }
}
