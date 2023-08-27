package arekkuusu.enderskills.api.event;

import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

public class SkillDurationEvent extends Event {

    private final EntityLivingBase entity;
    private final Skill skill;
    private int amount;

    public SkillDurationEvent(@Nullable EntityLivingBase entity, Skill skill, int amount) {
        this.entity = entity;
        this.skill = skill;
        this.amount = amount;
    }

    @Nullable
    public EntityLivingBase getEntityLiving() {
        return entity;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int value) {
        this.amount = value;
    }

    public static SkillDurationEvent trigger(EntityLivingBase owner, Skill skill, int original) {
        SkillDurationEvent event = new SkillDurationEvent(owner, skill, original);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
