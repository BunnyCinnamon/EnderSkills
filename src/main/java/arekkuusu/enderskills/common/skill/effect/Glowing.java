package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGlowing;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class Glowing extends BaseEffect implements IFindEntity, IExpand {

    public Glowing() {
        super(LibNames.GLOWING, new Properties());
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 20 * 4, 0));
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        entity.removeActivePotionEffect(MobEffects.GLOWING);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            entity.world.spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            entity.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData data) {
        SkillDamageSource skillSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        skillSource.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, skillSource, 8);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            target.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    public void activate(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(4 * 20)
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.SAME)
                .create();
        EntityPlaceableGlowing spawn = new EntityPlaceableGlowing(entity.world, SkillHelper.getOwner(status), status, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(entity.posX, entity.posY + entity.height / 2, entity.posZ);
        spawn.setRadius(2);
        spawn.growTicks = 5;
        entity.world.spawnEntity(spawn);
        unapply(entity);
        async(entity);
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(4 * 20)
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.SAME)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
