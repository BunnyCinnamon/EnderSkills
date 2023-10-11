package arekkuusu.enderskills.common.entity.throwable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.util.Vector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityThrowableFloatCustom extends EntityThrowableData {

    double accelerateX;
    double accelerateY;
    double accelerateZ;

    public EntityThrowableFloatCustom(World worldIn) {
        super(worldIn);
    }

    public EntityThrowableFloatCustom(World worldIn, EntityLivingBase owner, double distance, SkillData data, boolean gravity) {
        super(worldIn, owner, distance, data, gravity);
    }

    @Override
    public void onUpdate() {
        Vec3d pos = new Vec3d(accelerateX, accelerateY, accelerateZ);
        Vec3d currPos = getPositionEyes(1F);

        Vec3d diffPos = pos.subtract(currPos);
        diffPos = new Vector(diffPos).divide(diffPos.lengthVector()).toVec3d();

        double distEffect = 0.25D;
        double distEffectY = 0.5D * MathHelper.clamp((this.getDistance() / (this.startVector.distanceTo(getPositionVector()) + 1)), 0.1D, 1D);
        if (diffPos.lengthVector() > 2)
            distEffect = diffPos.lengthVector() / startVector.distanceTo(pos);
        if(distEffect > 0.6)
            distEffect = 0.6;
        double strength = (0.2D - 0.2D * distEffect);
        double strengthY = (0.3D - 0.3D * distEffectY);
        this.motionX += diffPos.x * strength;
        if (hasNoGravity())
            this.motionY += diffPos.y * strengthY;
        this.motionZ += diffPos.z * strength;
        this.motionX *= 1 - distEffect;
        if (hasNoGravity())
            this.motionY *= 1 - distEffectY;
        this.motionZ *= 1 - distEffect;
        super.onUpdate();
        if (ticksExisted < 20) {
            motionX += this.rand.nextGaussian() * 0.007499999832361937D * 5F;
            motionY += this.rand.nextGaussian() * 0.007499999832361937D * 5F;
            motionZ += this.rand.nextGaussian() * 0.007499999832361937D * 5F;
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        accelerateX = compound.getDouble("accelerateX");
        accelerateY = compound.getDouble("accelerateY");
        accelerateZ = compound.getDouble("accelerateZ");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setDouble("accelerateX", accelerateX);
        compound.setDouble("accelerateY", accelerateY);
        compound.setDouble("accelerateZ", accelerateZ);
    }

    public void throwAndSpawn(float velocity) {
        EntityLivingBase thrower = getEntityByUUID(getOwnerId());
        if (thrower != null) {
            this.rotationPitch = thrower.rotationPitch;
            this.rotationYaw = thrower.rotationYaw;
            thrower.world.spawnEntity(this);
        }
    }

    public static void throwFor(EntityLivingBase owner, double distance, SkillData data, double motionX, double motionY, double motionZ, boolean gravity) {
        EntityThrowableFloatCustom throwable = new EntityThrowableFloatCustom(owner.world, owner, distance, data, gravity);
        throwable.setOwnerId(owner.getUniqueID());
        throwable.throwAndSpawn(3F);
        throwable.accelerateX += motionX;
        throwable.accelerateY += motionY;
        throwable.accelerateZ += motionZ;
    }
}
