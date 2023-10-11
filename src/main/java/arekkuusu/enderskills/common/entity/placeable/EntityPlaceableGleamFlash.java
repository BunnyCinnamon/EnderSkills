package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.common.entity.data.IFlash;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableGleamFlash extends EntityPlaceableData {

    public int tickDelay;

    public EntityPlaceableGleamFlash(World world) {
        super(world);
    }

    public EntityPlaceableGleamFlash(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    public void onUpdate() {
        if (tickDelay > getData().nbt.getInteger("delay")) {
            if (tick == 0) {
                world.playSound(posX, posY, posZ, ModSounds.GLEAM_BANG_RELEASE, SoundCategory.PLAYERS, 5F, 1.0F, true);
                if (getData().skill instanceof IFlash) {
                    SkillData data = getData();
                    EntityLivingBase owner = SkillHelper.getOwner(data);
                    ((IFlash) getData().skill).onFlash(this, owner, data);
                }
            }
            super.onUpdate();
        } else {
            if (world.isRemote) {
                Vec3d pos = getPositionVector();
                double particlespeed = 0.15;
                Vec3d particlePos = new Vec3d(0, 0, 0.1);
                particlePos = particlePos.rotateYaw(rand.nextFloat() * 180f);
                particlePos = particlePos.rotatePitch(rand.nextFloat() * 360f);

                Vec3d velocity = particlePos.normalize();
                velocity = new Vec3d(
                        velocity.x * particlespeed,
                        velocity.y * particlespeed,
                        velocity.z * particlespeed
                );
                particlePos = particlePos.add(pos);

                world.spawnParticle(EnumParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
            }
            this.tickDelay++;
        }
    }

    @Override
    public float getEyeHeight() {
        return this.height * 0.5F;
    }

    @Override
    protected void setSize(float width, float height) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double w = width / 2D;
        double h = height / 2D;
        this.width = width;
        this.height = height;
        setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX - w, axisalignedbb.minY - h, axisalignedbb.minZ - w, axisalignedbb.minX + w, axisalignedbb.minY + h, axisalignedbb.minZ + w));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height / 2F;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - f1, z - (double) f, x + (double) f, y + f1, z + (double) f));
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }
}
