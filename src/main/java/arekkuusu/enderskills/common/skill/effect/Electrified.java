package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

public class Electrified extends BaseEffect {

    public Electrified() {
        super(LibNames.ELECTRIFIED, new Properties());
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if(isClientWorld(entity)) return;
        if(entity.isWet()) {
            EntityLivingBase owner = SkillHelper.getOwner(data);
            double damage = 4;
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
            source.setMagicDamage();
            SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
            MinecraftForge.EVENT_BUS.post(event);
            entity.attackEntityFrom(event.getSource(), (float) (event.getAmount() / data.time));
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id)
                .with(14 * 20)
                .put(data.nbt, data.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
