package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGlowing;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AbilityInfo(compound);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (entity.world.isRemote && entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
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
        SkillDamageSource skillSource = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        skillSource.setMagicDamage();

        double[] damage = new double[]{DSLDefaults.getDamage(this, 0)};
        Capabilities.get(owner).flatMap(skills -> skills.getOwned(this)).ifPresent(info -> {
            AbilityInfo abilityInfo = (AbilityInfo) info;

            damage[0] = DSLDefaults.getDamage(this, abilityInfo.getLevel());
        });
        SkillDamageEvent event = new SkillDamageEvent(owner, this, skillSource, damage[0]);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.getAmount() > 0) {
            target.attackEntityFrom(event.getSource(), event.toFloat());
        }

        SoundHelper.playSound(target.world, target.getPosition(), ModSounds.PASSIVE_POP);
    }

    public void activate(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(4 * 20)
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.SAME)
                .create();
        double[] radius = new double[]{DSLDefaults.getRange(this, 0)};
        Capabilities.get(SkillHelper.getOwner(data)).flatMap(skills -> skills.getOwned(this)).ifPresent(info -> {
            AbilityInfo abilityInfo = (AbilityInfo) info;

            radius[0] = DSLDefaults.getRange(this, abilityInfo.getLevel());
        });
        EntityPlaceableGlowing spawn = new EntityPlaceableGlowing(entity.world, SkillHelper.getOwner(status), status, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(entity.posX, entity.posY + entity.height / 2, entity.posZ);
        spawn.setRadius(radius[0]);
        spawn.growTicks = 5;
        entity.world.spawnEntity(spawn);
       super.unapply(entity);
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

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.GLOWING;

    @Config(modid = LibMod.MOD_ID, name = Glowing.CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(Glowing.CONFIG_FILE);
    }
}
