package arekkuusu.enderskills.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;

import javax.annotation.Nullable;

public class SkillDamageSource extends EntityDamageSource {

    public SkillDamageSource(String damageTypeIn, @Nullable Entity damageSourceEntityIn) {
        super(damageTypeIn, damageSourceEntityIn);
    }
}
