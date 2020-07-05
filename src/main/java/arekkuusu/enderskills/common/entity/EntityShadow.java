package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class EntityShadow extends Entity {

    public static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityShadow.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Float> MIRROR_DAMAGE = EntityDataManager.createKey(EntityShadow.class, DataSerializers.FLOAT);
    public static final DataParameter<Boolean> FADED = EntityDataManager.createKey(EntityShadow.class, DataSerializers.BOOLEAN);
    public WeakHashMap<EntityLivingBase, Float> attackMap = new WeakHashMap<>();
    public int fadedCountdown = 30;

    public EntityShadow(World world) {
        super(world);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            UUID uuid = getOwnerId();
            if (uuid != null) {
                EntityLivingBase owner = getEntityByUUID(uuid);
                if (owner != null) {
                    if (ticksExisted % 20 == 0) { //Check if skill is still active every 2 seconds
                        if (!SkillHelper.isActiveOwner(owner, ModAbilities.SHADOW)) {
                            setDead();
                        }
                    }
                    if (owner.getDistance(this) > 10) {
                        this.setPositionAndUpdate(owner.posX, owner.posY, owner.posZ);
                        setFaded(false);
                    }
                    if (ticksExisted % 10 == 0 && !attackMap.isEmpty()) {
                        for (Map.Entry<EntityLivingBase, Float> set : attackMap.entrySet()) {
                            EntityLivingBase entity = set.getKey();
                            float damage = set.getValue() + (set.getValue() * getMirrorDamage());
                            SkillDamageSource source = new SkillDamageSource("skill", owner);
                            SkillDamageEvent event = new SkillDamageEvent(owner, ModAbilities.SHADOW, source, damage);
                            MinecraftForge.EVENT_BUS.post(event);
                            entity.attackEntityFrom(event.getSource(), event.toFloat());

                            if (entity.world instanceof WorldServer) {
                                ((WorldServer) entity.world).playSound(null, entity.posX, entity.posY, entity.posZ, ModSounds.SHADOW_ATTACK, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
                            }
                        }
                        attackMap.clear();
                    }
                } else setDead();
            }
        }
        if (world.isRemote && fadedCountdown > 0 && ticksExisted % 2 == 0 && world.rand.nextDouble() < 0.8D) {
            double posX = this.posX + world.rand.nextDouble() - 0.5D;
            double posY = this.posY + getEyeHeight() * world.rand.nextDouble();
            double posZ = this.posZ + world.rand.nextDouble() - 0.5D;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0, 0, 0);
        }
        if (fadedCountdown > 0) --fadedCountdown;
        if (!isFaded() && fadedCountdown == 0) {
            fadedCountdown = 30;
            if (!world.isRemote) {
                setFaded(true);
            }
        }
    }

    public void addAttack(EntityLivingBase entity, float amount) {
        if (attackMap.containsKey(entity)) {
            attackMap.put(entity, attackMap.get(entity) + amount);
        } else {
            attackMap.put(entity, amount);
        }
    }

    public void teleportNextToOwner() {
        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                Vec3d eyesVector = owner.getPositionVector();
                Vec3d lookVector = owner.getLook(1F).scale(-1F);
                lookVector = lookVector.rotateYaw(rand.nextBoolean() ? -40F : 40F);
                Vec3d targetVector = eyesVector.addVector(
                        lookVector.x,
                        lookVector.y,
                        lookVector.z
                );
                setPositionAndUpdate(targetVector.x, targetVector.y, targetVector.z);
                setFaded(false);
            }
        }
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        return null;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
        this.dataManager.register(MIRROR_DAMAGE, 0F);
        this.dataManager.register(FADED, false);
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setOwnerId(@Nullable UUID owner) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(owner));
    }

    public float getMirrorDamage() {
        return this.dataManager.get(MIRROR_DAMAGE);
    }

    public void setMirrorDamage(float damage) {
        this.dataManager.set(MIRROR_DAMAGE, damage);
    }

    public boolean isFaded() {
        return this.dataManager.get(FADED);
    }

    public void setFaded(boolean faded) {
        this.dataManager.set(FADED, faded);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setMirrorDamage(compound.getFloat("mirror"));
        fadedCountdown = 30;
        String s;

        if (compound.hasKey("OwnerUUID", 8)) {
            s = compound.getString("OwnerUUID");
        } else {
            String s1 = compound.getString("Owner");
            s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
        }

        if (!s.isEmpty()) {
            try {
                this.setOwnerId(UUID.fromString(s));
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("mirror", getMirrorDamage());
        if (this.getOwnerId() == null) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.getOwnerId().toString());
        }
    }
}
