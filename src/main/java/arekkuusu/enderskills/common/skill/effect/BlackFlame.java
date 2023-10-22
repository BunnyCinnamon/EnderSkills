package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

public class BlackFlame extends BaseEffect {

    public BlackFlame() {
        super(LibNames.BLACK_FLAME, new Properties());
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (!entity.world.isRemote) {
            EntityLivingBase owner = SkillHelper.getOwner(data);
            double damage = data.nbt.getDouble("dot");
            double true_damage = data.nbt.getDouble("true_dot");
            int time = data.time;
            BlackFlame.dealDamage(this, entity, owner, damage, time);
            BlackFlame.dealTrueDamage(this, entity, owner, true_damage, time);
        } else if (entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d((1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05), 2F, 10, 0xFFFFFF, ResourceLibrary.GLOW_NEGATIVE);
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d((1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05), 2F, 10, 0xFFFFFF, ResourceLibrary.GLOW_NEGATIVE);
        }
    }

    public static void dealDamage(Skill skill, EntityLivingBase entity, EntityLivingBase owner, double damage, int time) {
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        source.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, skill, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0) {
            entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) (event.getAmount() / time));
        }
    }

    public static void dealTrueDamage(Skill skill, EntityLivingBase entity, EntityLivingBase owner, double damage, int time) {
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        source.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, skill, source, entity.getMaxHealth() * damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0) {
            entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) (event.getAmount() / time));
        }
    }

    public static void dealDamage(Skill skill, EntityLivingBase entity, EntityLivingBase owner, double damage) {
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        source.setMagicDamage();
        SkillDamageEvent event = new SkillDamageEvent(owner, skill, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0) {
            entity.attackEntityFrom(event.getSource(), event.toFloat());
        }
    }

    public static void dealTrueDamage(Skill skill, EntityLivingBase entity, EntityLivingBase owner, double damage) {
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, skill, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0) {
            entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, event.toFloat());
        }
    }

    public static void dealTrueDamageHAHAHA(Skill skill, EntityLivingBase entity, EntityLivingBase owner, double damage) {
        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, skill, source, entity.getMaxHealth() * damage);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getAmount() > 0) {
            entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, event.toFloat());
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
       super.sync(entity, status);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLACK_FLAME_OFFENCE_CONFIG + LibNames.BLACK_FLAME;

    @Config(modid = LibMod.MOD_ID, name = BlackFlame.CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(BlackFlame.CONFIG_FILE);
    }
}
