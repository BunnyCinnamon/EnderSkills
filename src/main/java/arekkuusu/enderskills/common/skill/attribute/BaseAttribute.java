package arekkuusu.enderskills.common.skill.attribute;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.skill.BaseSkill;
import arekkuusu.enderskills.common.skill.ModAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseAttribute extends BaseSkill {

    public BaseAttribute(String id, Properties properties) {
        super(properties.setHasTexture());
        ModAttributes.setRegistry(this, id);
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    @Override
    public boolean canUpgrade(EntityLivingBase entity) {
        return Capabilities.advancement(entity).map(c -> {
            ISkillAdvancement.Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            return c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            ISkillAdvancement.Requirement requirement = getRequirement(entity);
            int xp = requirement.getXp();
            if (c.getExperienceTotal(entity) >= xp) {
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public ISkillAdvancement.Requirement getRequirement(EntityLivingBase entity) {
        AttributeInfo info = (AttributeInfo) Capabilities.get(entity).flatMap(a -> a.getOwned(this)).orElse(null);
        return new ISkillAdvancement.DefaultRequirement(0, getUpgradeCost(info));
    }

    public int getUpgradeCost(@Nullable AttributeInfo info) {
        int lvl = info != null ? info.getLevel() + 1 : 0;
        return (int) (getExperience(lvl) * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }

    public abstract double getExperience(int lvl);

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AttributeInfo(compound);
    }
}
