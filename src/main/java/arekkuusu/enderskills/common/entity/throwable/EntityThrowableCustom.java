package arekkuusu.enderskills.common.entity.throwable;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.util.Vector;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public abstract class EntityThrowableCustom extends Entity implements IProjectile {

    public static final DataParameter<Float> DISTANCE = EntityDataManager.createKey(EntityThrowableCustom.class, DataSerializers.FLOAT);
    public static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityThrowableCustom.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Optional<UUID>> FOLLOW_UNIQUE_ID = EntityDataManager.createKey(EntityThrowableCustom.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public Vec3d startVector = Vec3d.ZERO;
    public int throwableShake;
    public int ticksInAir;

    public EntityThrowableCustom(World worldIn) {
        super(worldIn);
        setNoGravity(true);
        setSize(0.25F, 0.25F);
    }

    public EntityThrowableCustom(World worldIn, EntityLivingBase owner, int distance) {
        this(worldIn);
        Vec3d posVec = new Vec3d(owner.posX, owner.posY + owner.getEyeHeight() - 0.10000000149011612D, owner.posZ);
        Vec3d lookVec = owner.getLookVec().normalize();
        posVec = posVec.addVector(
                lookVec.x * 0.5,
                lookVec.y * 0.5,
                lookVec.z * 0.5
        );
        setPosition(posVec.x, posVec.y, posVec.z);
        setNoGravity(true);
        setDistance(distance);
        this.startVector = posVec;
    }

    @Override
    public void entityInit() {
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
        this.dataManager.register(FOLLOW_UNIQUE_ID, Optional.absent());
        this.dataManager.register(DISTANCE, 0F);
    }

    public abstract void onImpact(RayTraceResult result);

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, double velocity, double inaccuracy) {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot(f, f1, f2, (float) velocity, (float) inaccuracy);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround) {
            this.motionY += entityThrower.motionY;
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double) f;
        y = y / (double) f;
        z = z / (double) f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        x = x * velocity;
        y = y * velocity;
        z = z * velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        rotateTowardsMovement(this, 1F);
        EntityLivingBase thrower = getEntityByUUID(getOwnerId());
        if (this.world.isRemote || (thrower == null || !thrower.isDead) && this.world.isBlockLoaded(new BlockPos(this))) {
            ++this.ticksInAir;
            if (this.getDistance() <= this.startVector.distanceTo(getPositionVector())) {
                if (!world.isRemote) {
                    RayTraceResult missResult = new RayTraceResult(RayTraceResult.Type.MISS, getPositionVector(), null, null);
                    onImpact(missResult);
                    setDead();
                }
            } else {
                if (!world.isRemote) {
                    RayTraceResult raytraceresult = RayTraceHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, thrower);
                    if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                        onImpact(raytraceresult);
                        setDead();
                    }
                }
                if (getFollowId() != null) {
                    EntityLivingBase follow = getEntityByUUID(getFollowId());
                    if (follow != null && !follow.isDead) {
                        Vec3d pos = follow.getPositionEyes(1F);
                        Vec3d mov = new Vec3d(follow.motionX, follow.motionY, follow.motionZ);
                        Vec3d nextPos = pos.add(mov);
                        Vec3d currPos = getPositionEyes(1F);

                        Vec3d diffPos = nextPos.subtract(currPos);
                        diffPos = new Vector(diffPos).divide(diffPos.lengthVector()).toVec3d();

                        double distEffect = 0.25D;
                        if (diffPos.lengthVector() > 2)
                            distEffect = diffPos.lengthVector() / startVector.distanceTo(pos);
                        if(distEffect > 0.6)
                            distEffect = 0.6;
                        double strength = (0.2D - 0.2D * distEffect);
                        this.motionX += diffPos.x * strength;
                        if (hasNoGravity())
                            this.motionY += diffPos.y * strength;
                        this.motionZ += diffPos.z * strength;
                        this.motionX *= 1 - distEffect;
                        if (hasNoGravity())
                            this.motionY *= 1 - distEffect;
                        this.motionZ *= 1 - distEffect;
                    }
                }

                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
                if (!this.hasNoGravity()) {
                    this.motionY -= 0.09000000074505806D;
                }
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        } else {
            this.setDead();
        }
    }

    public void rotateTowardsMovement(Entity projectile, float rotationSpeed) {
        double d0 = -projectile.motionX;
        double d1 = -projectile.motionY;
        double d2 = -projectile.motionZ;
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2);
        projectile.rotationYaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) + 90.0F;
        projectile.rotationPitch = (float) (MathHelper.atan2((double) f, d1) * (180D / Math.PI)) - 90.0F;
        while (projectile.rotationPitch - projectile.prevRotationPitch < -180.0F) {
            projectile.prevRotationPitch -= 360.0F;
        }

        while (projectile.rotationPitch - projectile.prevRotationPitch >= 180.0F) {
            projectile.prevRotationPitch += 360.0F;
        }

        while (projectile.rotationYaw - projectile.prevRotationYaw < -180.0F) {
            projectile.prevRotationYaw -= 360.0F;
        }

        while (projectile.rotationYaw - projectile.prevRotationYaw >= 180.0F) {
            projectile.prevRotationYaw += 360.0F;
        }

        projectile.rotationPitch = projectile.prevRotationPitch + (projectile.rotationPitch - projectile.prevRotationPitch) * rotationSpeed;
        projectile.rotationYaw = projectile.prevRotationYaw + (projectile.rotationYaw - projectile.prevRotationYaw) * rotationSpeed;
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        return null;
    }

    @Nullable
    public UUID getFollowId() {
        return this.dataManager.get(FOLLOW_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setFollowId(@Nullable UUID owner) {
        this.dataManager.set(FOLLOW_UNIQUE_ID, Optional.fromNullable(owner));
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setOwnerId(@Nullable UUID owner) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(owner));
    }

    @Override
    public float getEyeHeight() {
        return this.height / 2;
    }

    public void setDistance(float distance) {
        dataManager.set(DISTANCE, distance);
    }

    public float getDistance() {
        return dataManager.get(DISTANCE);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("distance", getDistance());
        compound.setByte("shake", (byte) this.throwableShake);
        compound.setUniqueId("thrower", getOwnerId());
        NBTHelper.setVector(compound, "startVector", this.startVector);
        if (getFollowId() != null) {
            compound.setUniqueId("follow", getFollowId());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        setDistance(compound.getFloat("distance"));
        this.throwableShake = compound.getByte("shake") & 255;
        this.startVector = NBTHelper.getVector(compound, "startVector");
        setOwnerId(compound.getUniqueId("thrower"));
        if (compound.hasKey("follow")) {
            setFollowId(compound.getUniqueId("follow"));
        }
    }
}
