package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLivingBase;

public class Blinded extends BaseEffect {

    public Blinded() {
        super(LibNames.BLINDED, new Properties());
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id)
                .with(5)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
       super.apply(entity, status);
        sync(entity, status);
    }
}
