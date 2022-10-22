package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGlowing;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.IConfigSync;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Glowing extends BaseEffect implements IFindEntity, IExpand, IConfigSync {

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
        SkillDamageSource skillSource = new SkillDamageSource(BaseAbility.DAMAGE_DOT_TYPE, owner);
        skillSource.setMagicDamage();

        double[] damage = new double[]{this.config.get(this, "DAMAGE", 0)};
        Capabilities.get(owner).flatMap(skills -> skills.getOwned(this)).ifPresent(info -> {
            AbilityInfo abilityInfo = (AbilityInfo) info;

            damage[0] = this.config.get(this, "DAMAGE", abilityInfo.getLevel());
        });
        SkillDamageEvent event = new SkillDamageEvent(owner, this, skillSource, damage[0]);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
            target.attackEntityFrom(event.getSource(), event.toFloat());
        }

        if (target.world instanceof WorldServer) {
            ((WorldServer) target.world).playSound(null, target.posX, target.posY, target.posZ, ModSounds.PASSIVE_POP, SoundCategory.PLAYERS, 1.0F, (1.0F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    public void activate(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(4 * 20)
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.SAME)
                .create();
        double[] radius = new double[]{this.config.get(this, "RANGE", 0)};
        Capabilities.get(SkillHelper.getOwner(data)).flatMap(skills -> skills.getOwned(this)).ifPresent(info -> {
            AbilityInfo abilityInfo = (AbilityInfo) info;

            radius[0] = this.config.get(this, "RANGE", abilityInfo.getLevel());
        });
        EntityPlaceableGlowing spawn = new EntityPlaceableGlowing(entity.world, SkillHelper.getOwner(status), status, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(entity.posX, entity.posY + entity.height / 2, entity.posZ);
        spawn.setRadius(radius[0]);
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

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.GLOWING;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static final Configuration.Values CONFIG = new Configuration.Values();

        public static class Values {

            public String[] dsl = {
                    "",
                    "│ ",
                    "│ min_level: 0",
                    "│ max_level: infinite",
                    "│ ",
                    "",
                    "┌ DAMAGE (",
                    "│     shape: none",
                    "│     value: 8h",
                    "└ )",
                    "",
                    "┌ RANGE (",
                    "│     shape: none",
                    "│     value: 2b",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
