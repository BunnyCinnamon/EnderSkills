package arekkuusu.enderskills.api.event;

import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SkillDamageEvent extends Event {

    private final EntityLivingBase entity;
    private final DamageSource source;
    private final Skill skill;
    private double amount;

    public SkillDamageEvent(EntityLivingBase entity, Skill skill, DamageSource source, double amount) {
        this.entity = entity;
        this.source = source;
        this.skill = skill;
        this.amount = amount;
    }

    public EntityLivingBase getEntityLiving() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
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
}
