package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

public class Burning extends BaseEffect {

    public Burning() {
        super(LibNames.BURNING, new Properties());
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("dot");
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        source.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        entity.attackEntityFrom(event.getSource(), (float) (event.getAmount() / data.time));
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(data.nbt.getInteger("dotDuration"))
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
