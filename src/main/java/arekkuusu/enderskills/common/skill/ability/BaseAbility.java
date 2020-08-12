package arekkuusu.enderskills.common.skill.ability;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.event.SkillsActionableEvent;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.BaseSkill;
import arekkuusu.enderskills.common.skill.ModAbilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class BaseAbility extends BaseSkill {

    public static final String DAMAGE_HIT_TYPE = "skill";
    public static final String DAMAGE_DOT_TYPE = "indirectSkill";

    public BaseAbility(String id, Properties properties) {
        super(properties.setHasStatusIcon().setKeyBound().setHasTexture());
        ModAbilities.setRegistry(this, id);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        update(entity, data, 0);
    }

    @Override
    public boolean canUpgrade(EntityLivingBase entity) {
        return Capabilities.advancement(entity).map(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            return c.level >= tokens && c.getExperienceTotal(entity) >= xp;
        }).orElse(false);
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            if (c.level >= tokens && c.getExperienceTotal(entity) >= xp) {
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public Requirement getRequirement(EntityLivingBase entity) {
        AbilityInfo info = (AbilityInfo) Capabilities.get(entity).flatMap(a -> a.getOwned(this)).orElse(null);
        int tokensNeeded = 0;
        int xpNeeded;
        if (info == null) {
            int abilities = Capabilities.get(entity).map(c -> (int) c.getAllOwned().keySet().stream().filter(s -> s instanceof BaseAbility).count()).orElse(0);
            if (abilities > 0) {
                tokensNeeded = abilities + 1;
            } else {
                tokensNeeded = 1;
            }
        }
        xpNeeded = getUpgradeCost(info);
        return new DefaultRequirement(tokensNeeded, getCostIncrement(entity, xpNeeded));
    }

    public abstract int getCostIncrement(EntityLivingBase entity, int total);

    public abstract int getUpgradeCost(@Nullable AbilityInfo info);

    public boolean isActionable(EntityLivingBase entity) {
        SkillsActionableEvent event = new SkillsActionableEvent(entity, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean canActivate(EntityLivingBase entity) {
        SkillActivateEvent event = new SkillActivateEvent(entity, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AbilityInfo(compound);
    }

    public static class AbilityProperties extends BaseProperties {

        public Function<AbilityInfo, Integer> cooldownFunction;

        public AbilityProperties setCooldownGetter(Function<AbilityInfo, Integer> cooldownFunction) {
            this.cooldownFunction = cooldownFunction;
            return this;
        }

        public int getCooldown(AbilityInfo abilityInfo) {
            return cooldownFunction.apply(abilityInfo);
        }
    }
}
