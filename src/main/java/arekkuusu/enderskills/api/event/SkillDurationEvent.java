package arekkuusu.enderskills.api.event;

import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

public class SkillDurationEvent extends Event {

    private final EntityLivingBase entity;
    private final Skill skill;
    private double amount;

    public SkillDurationEvent(@Nullable EntityLivingBase entity, Skill skill, double amount) {
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double value) {
        this.amount = value;
    }

    public float toFloat() {
        return (float) getAmount();
    }

    public static double getDuration(EntityLivingBase owner, Skill skill, double original) {
        SkillDurationEvent event = new SkillDurationEvent(owner, skill, original);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }
}
