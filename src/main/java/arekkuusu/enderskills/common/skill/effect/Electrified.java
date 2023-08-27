package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Electrified extends BaseEffect {

    public Electrified() {
        super(LibNames.ELECTRIFIED, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        if (SkillHelper.isActive(entity, ModEffects.STUNNED)) {
           super.unapply(entity, data);
            async(entity, data);
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        entity.getEntityData().setBoolean(LibMod.MOD_ID + ":propagated", false);
    }

    public List<Delay> propagators = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSeverUpdate(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Iterator<Delay> iterator = this.propagators.iterator();
            while (iterator.hasNext()) {
                Delay delay = iterator.next();
                if (delay.i++ > 10) {
                    if (canArc(delay.entityTo, delay.data)) {
                        //Spawn lightning!
                        delay.entityTo.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 1);
                        ModEffects.STUNNED.set(delay.entityTo, delay.data, delay.time);
                        EnderSkills.getProxy().addToQueue(delay::apply);
                        EntityLivingBase from = delay.entityFrom;
                        EntityLivingBase to = delay.entityTo;
                        Vector posFrom = new Vector(from.getPositionVector()).addVector(from.world.rand.nextDouble() * 0.05D, from.height / 2D + from.world.rand.nextDouble() * 0.05D, from.world.rand.nextDouble() * 0.05D);
                        Vector posTo = new Vector(to.getPositionVector()).addVector(to.world.rand.nextDouble() * 0.05D, to.height / 2D + to.world.rand.nextDouble() * 0.05D, to.world.rand.nextDouble() * 0.05D);
                        EnderSkills.getProxy().spawnLightning(to.world, posFrom, posTo, 4, 0.6F, 5, 0xF4F389, false);

                        if (to.world instanceof WorldServer) {
                            ((WorldServer) to.world).playSound(null, posTo.x, posTo.y, posTo.z, ModSounds.ELECTRIC_HIT, SoundCategory.BLOCKS, 0.5F, (1.0F + (to.world.rand.nextFloat() - to.world.rand.nextFloat()) * 0.2F) * 0.7F);
                            ((WorldServer) to.world).playSound(null, posTo.x, posTo.y, posTo.z, ModSounds.ELECTRIC_STUN, SoundCategory.BLOCKS, 0.5F, (1.0F + (to.world.rand.nextFloat() - to.world.rand.nextFloat()) * 0.2F) * 0.7F);
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }

    public void propagate(EntityLivingBase source, SkillData data, int time) {
        ModEffects.STUNNED.set(source, data, time);
        if (source.world instanceof WorldServer) {
            ((WorldServer) source.world).playSound(null, source.posX, source.posY, source.posZ, ModSounds.ELECTRIC_STUN, SoundCategory.BLOCKS, 0.5F, (1.0F + (source.world.rand.nextFloat() - source.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
        this.arc(source, data, time);
    }

    public void arc(EntityLivingBase source, SkillData data, int time) {
        source.world.getEntitiesWithinAABBExcludingEntity(source, source.getEntityBoundingBox().grow(1.5)).stream().filter(e -> canArc(e, data)).limit(1 + source.world.rand.nextInt(1)).forEach(entity -> {
            this.propagators.add(new Delay(source, (EntityLivingBase) entity, data, time));
        });
    }

    public boolean canArc(Entity entity, SkillData data) {
        return SkillHelper.isActive(entity, this) && !SkillHelper.isActive(entity, ModEffects.STUNNED, data.id + ":" + data.skill.getRegistryName());
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id)
                .with(14 * 20)
                .put(data.nbt, data.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
       super.apply(entity, status);
        sync(entity, status);
    }

    public static class Delay {

        EntityLivingBase entityFrom;
        EntityLivingBase entityTo;
        SkillData data;
        int time;
        int i;

        public Delay(EntityLivingBase entityFrom, EntityLivingBase entityTo, SkillData data, int time) {
            this.entityFrom = entityFrom;
            this.entityTo = entityTo;
            this.data = data;
            this.time = time;
        }

        public void apply() {
            ModEffects.ELECTRIFIED.arc(entityTo, data, time);
        }
    }
}
