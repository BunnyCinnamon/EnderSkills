package arekkuusu.enderskills.common.skill.attribute;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.client.gui.data.SkillAdvancement;
import arekkuusu.enderskills.common.CommonConfig;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public class AttributeSkillAdvancement {

    private final BaseAttribute baseAttribute;

    public AttributeSkillAdvancement(BaseAttribute baseAttribute) {
        this.baseAttribute = baseAttribute;
    }

    @Override
    public boolean canUpgrade(EntityLivingBase entity) {
        return Capabilities.advancement(entity).map(c -> {
            SkillAdvancement.Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            return c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            SkillAdvancement.Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            if (c.getExperienceTotal(entity) >= xp) {
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public SkillAdvancement.Requirement getRequirement(EntityLivingBase entity) {
        AttributeInfo info = (AttributeInfo) Capabilities.get(entity).flatMap(a -> a.getOwned(this.baseAttribute)).orElse(null);
        return new SkillAdvancement.DefaultRequirement(0, getUpgradeCost(info));
    }

    public int getUpgradeCost(@Nullable AttributeInfo info) {
        int lvl = info != null ? info.getLevel() + 1 : 0;
        return (int) (DSLDefaults.getExperience(this.baseAttribute, lvl) * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }
}