package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.client.render.skill.LumenWaveRenderer;
import arekkuusu.enderskills.common.skill.SkillHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableLumenWave extends EntityPlaceableData {

    public EntityPlaceableLumenWave(World world) {
        super(world);
    }

    public EntityPlaceableLumenWave(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ignoreFrustumCheck = true;
        noClip = true;
        if (!world.isRemote) {
            if (motionY != 0) {
                RayTraceResult raytraceresult = RayTraceHelper.forwardsRaycast(this, true, this.tick >= 25, SkillHelper.getOwner(getData()));
                if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    if (raytraceresult.typeOfHit == RayTraceResult.Type.ENTITY) {
                        setPosition(raytraceresult.entityHit.posX, raytraceresult.entityHit.posY + 1, raytraceresult.entityHit.posZ);
                    } else {
                        setPosition(raytraceresult.hitVec.x, raytraceresult.hitVec.y + (raytraceresult.sideHit == EnumFacing.UP ? 1 : -1), raytraceresult.hitVec.z);
                    }
                    motionY = 0;
                }
            }
            move(MoverType.SELF, motionX, motionY, motionZ);
            markVelocityChanged();
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
