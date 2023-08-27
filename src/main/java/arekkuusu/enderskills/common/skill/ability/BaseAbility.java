package arekkuusu.enderskills.common.skill.ability;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ModAbilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public abstract class BaseAbility extends Skill {

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

    public boolean hasCooldown(SkillInfo skillInfo) {
        return skillInfo instanceof InfoCooldown && ((InfoCooldown) skillInfo).hasCooldown();
    }

    public boolean hasNoCooldown(SkillInfo skillInfo) {
        return !hasCooldown(skillInfo);
    }

    public boolean isActionable(EntityLivingBase entityLivingBase) {
        SkillActionableEvent event = new SkillActionableEvent(entityLivingBase, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean isNotActionable(EntityLivingBase entityLivingBase) {
        return !isActionable(entityLivingBase);
    }

    public boolean canActivate(EntityLivingBase entityLivingBase) {
        SkillActivateEvent event = new SkillActivateEvent(entityLivingBase, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean canNotActivate(EntityLivingBase entityLivingBase) {
        return !canActivate(entityLivingBase);
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AbilityInfo(compound);
    }
}
