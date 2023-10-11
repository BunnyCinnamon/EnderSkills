package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

public class Burning extends BaseEffect {

    public Burning() {
        super(LibNames.BURNING, new Properties());
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (!entity.world.isRemote) {
            EntityLivingBase owner = SkillHelper.getOwner(data);
            double damage = data.nbt.getDouble("dot");
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
            source.setFireDamage();
            SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
            MinecraftForge.EVENT_BUS.post(event);
            if(event.getAmount() > 0) {
                entity.attackEntityFrom(event.getSource(), (float) (event.getAmount() / data.time));
            }
        } else if (entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            entity.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(data.nbt.getInteger("dotDuration"))
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
       super.apply(entity, status);
        sync(entity, status);
    }
}
