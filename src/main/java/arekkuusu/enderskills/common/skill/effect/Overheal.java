package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class Overheal extends BaseEffect {

    public Overheal() {
        super(LibNames.OVERHEAL, new Properties());
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        float overHeal = data.nbt.getFloat("over_heal");
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + overHeal);
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        float overHeal = data.nbt.getFloat("over_heal");
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - overHeal);
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        this.set(entity, 0F);
    }

    public void set(EntityLivingBase entity, float amount) {
        float maxHeal = entity.getMaxHealth();
        float health = entity.getHealth();
        float remainingHeal = MathHelper.clamp((amount + health) - maxHeal, 0, maxHeal * 1.5F);
        entity.heal(amount);
        if (remainingHeal > 0) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setFloat(compound, "over_heal", remainingHeal);
            SkillData data = SkillData.of(this)
                    .with(5 * 20)
                    .put(compound)
                    .create();
           super.apply(entity, data);
            sync(entity, data);
        }
    }
}
