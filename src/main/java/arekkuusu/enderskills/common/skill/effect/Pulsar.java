package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class Pulsar extends BaseEffect implements IExpand, IFindEntity {

    public Pulsar() {
        super(LibNames.PULSAR, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        int pulseTime = NBTHelper.getInteger(data.nbt, "pulseTime");
        if (!isClientWorld(entity)) {
            EntityLivingBase owner = SkillHelper.getOwner(data);
            if (owner == null || !isWithinEffectiveDistance(entity, owner)) {
                unapply(entity, data);
                async(entity, data);
            } else if (tick % pulseTime == 0) {
                double radius = NBTHelper.getDouble(data.nbt, "pulseRange");
                EntityPlaceableData spawn = new EntityPlaceableData(entity.world, entity, data, EntityPlaceableData.MIN_TIME);
                spawn.setPosition(entity.posX, entity.posY + entity.height / 2, entity.posZ);
                spawn.setRadius(radius);
                entity.world.spawnEntity(spawn);

                if (spawn.world instanceof WorldServer) {
                    ((WorldServer) spawn.world).playSound(null, spawn.posX, spawn.posY, spawn.posZ, ModSounds.HOME_STAR_BOOM, SoundCategory.PLAYERS, 1.0F, (1.0F + (spawn.world.rand.nextFloat() - spawn.world.rand.nextFloat()) * 0.2F) * 0.7F);
                }
            }
        }
    }

    public boolean isWithinEffectiveDistance(EntityLivingBase entity, EntityLivingBase owner) {
        SkillData data = SkillHelper.getActive(owner, ModAbilities.HOME_STAR, owner.getUniqueID().toString()).orElse(null);
        if(data == null) return false;
        double time = NBTHelper.getInteger(data.nbt, "time");
        double tick = NBTHelper.getInteger(data.nbt, "tick");
        double progress = MathHelper.clamp((double) tick / Math.min(time, EntityPlaceableData.MIN_TIME), 0D, 1D);
        double distance = NBTHelper.getDouble(data.nbt, "range") * progress;
        return distance >= owner.getDistance(entity);
    }

    //* Entity *//
    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        double damage = skillData.nbt.getDouble("damage");
        SkillDamageSource damageSource = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        damageSource.setExplosion();
        SkillDamageEvent event = new SkillDamageEvent(owner, this, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        target.attackEntityFrom(event.getSource(), (float) event.getAmount());
        EnderSkills.getProxy().addToQueue(() -> {
            ModEffects.BURNING.set(target, skillData);
        });
        pushEntity(source, target);

        if (target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.FIRE_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    public void pushEntity(Entity pusher, Entity pushed) {
        Vec3d pusherPos = pusher.getPositionVector();
        Vec3d pushedPos = pushed.getPositionVector();
        Vec3d motion = pusherPos.subtract(pushedPos).normalize().scale(0.2D);
        pushed.motionX += motion.x;
        pushed.motionY += motion.y;
        pushed.motionZ += motion.z;
    }
    //* Entity *//

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || !event.getSource().isFireDamage()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (SkillHelper.isActive(entity, this)) {
            event.setAmount(0);
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(ModEffects.PULSAR)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(data.nbt.getInteger("time"))
                .overrides(SkillData.Overrides.SAME)
                .put(data.nbt.copy(), data.watcher.copy())
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
