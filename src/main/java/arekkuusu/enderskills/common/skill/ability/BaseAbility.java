package arekkuusu.enderskills.common.skill.ability;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillShouldUseEvent;
import arekkuusu.enderskills.api.event.SkillUseEvent;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public abstract class BaseAbility extends Skill {

    public static final String DAMAGE_TYPE = "skill";
    public static final int INDEFINITE = -1;
    public static final int INSTANT = 0;

    public BaseAbility(String id) {
        ModAbilities.setRegistry(this, id);
    }

    public boolean shouldUse(EntityLivingBase entity) {
        SkillShouldUseEvent event = new SkillShouldUseEvent(entity, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean canUse(EntityLivingBase entity) {
        SkillUseEvent event = new SkillUseEvent(entity, this);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public void unapply(EntityLivingBase entity, SkillData data) {
        Capabilities.get(entity).ifPresent(skills -> skills.deactivate(this, h -> h.data.equals(data))); //Remove from entity Server Side
    }

    public void async(EntityLivingBase entity, SkillData data) {
        PacketHelper.sendSkillDataRemoveResponsePacket(entity, data); //Send to Client
    }

    public abstract int getCooldown(AbilityInfo info);

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AbilityInfo(compound);
    }
}
