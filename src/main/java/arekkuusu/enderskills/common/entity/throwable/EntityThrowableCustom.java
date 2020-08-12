package arekkuusu.enderskills.common.entity.throwable;

import arekkuusu.enderskills.api.helper.RayTraceHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public abstract class EntityThrowableCustom extends Entity implements IProjectile {

    public static final DataParameter<Integer> LIFE_TIME = EntityDataManager.createKey(EntityThrowableCustom.class, DataSerializers.VARINT);
    public EntityLivingBase thrower;
    public int throwableShake;
    public String throwerName;
    public int ticksInAir;

    public EntityThrowableCustom(World worldIn) {
        super(worldIn);
        setNoGravity(true);
        setSize(0.25F, 0.25F);
    }

    public EntityThrowableCustom(World worldIn, EntityLivingBase owner, int lifeTime) {
        this(worldIn);
        Vec3d posVec = new Vec3d(owner.posX, owner.posY + owner.getEyeHeight() - 0.10000000149011612D, owner.posZ);
        Vec3d lookVec = owner.getLookVec().normalize();
        posVec = posVec.addVector(
                lookVec.x * 0.5,
                lookVec.y * 0.5,
                lookVec.z * 0.5
        );
        this.thrower = owner;
        setPosition(posVec.x, posVec.y, posVec.z);
        setNoGravity(true);
        setLifeTime(lifeTime);
    }

    public void entityInit() {
        this.dataManager.register(LIFE_TIME, 0);
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
        if (this.world.isRemote || (this.thrower == null || !this.thrower.isDead) && this.world.isBlockLoaded(new BlockPos(this))) {
            ++this.ticksInAir;
            if (this.getLifeTime() <= ticksInAir) {
                if (!world.isRemote) {
                    RayTraceResult missResult = new RayTraceResult(RayTraceResult.Type.MISS, getPositionVector(), null, null);
                    onImpact(missResult);
                    setDead();
                }
            } else {
                if (!world.isRemote) {
                    RayTraceResult raytraceresult = RayTraceHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.thrower);
                    if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                        onImpact(raytraceresult);
                        setDead();
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

    @Override
    public float getEyeHeight() {
        return this.height / 2;
    }

    public void setLifeTime(int age) {
        dataManager.set(LIFE_TIME, age);
    }

    public int getLifeTime() {
        return dataManager.get(LIFE_TIME);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("lifeTime", getLifeTime());
        compound.setByte("shake", (byte) this.throwableShake);

        if ((this.throwerName == null || this.throwerName.isEmpty()) && this.thrower instanceof EntityPlayer) {
            this.throwerName = this.thrower.getName();
        }

        compound.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        setLifeTime(compound.getInteger("lifeTime"));
        this.throwableShake = compound.getByte("shake") & 255;
        this.thrower = null;
        this.throwerName = compound.getString("ownerName");

        if (this.throwerName != null && this.throwerName.isEmpty()) {
            this.throwerName = null;
        }

        this.thrower = this.getThrower();
    }

    @Nullable
    public EntityLivingBase getThrower() {
        if (this.thrower == null && this.throwerName != null && !this.throwerName.isEmpty()) {
            this.thrower = this.world.getPlayerEntityByName(this.throwerName);

            if (this.thrower == null && this.world instanceof WorldServer) {
                try {
                    Entity entity = ((WorldServer) this.world).getEntityFromUuid(UUID.fromString(this.throwerName));

                    if (entity instanceof EntityLivingBase) {
                        this.thrower = (EntityLivingBase) entity;
                    }
                } catch (Throwable var2) {
                    this.thrower = null;
                }
            }
        }

        return this.thrower;
    }
}
