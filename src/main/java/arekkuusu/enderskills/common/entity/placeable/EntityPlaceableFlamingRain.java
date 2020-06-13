package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlaceableFlamingRain extends EntityPlaceableData {

    public EntityPlaceableFlamingRain(World world) {
        super(world);
    }

    public EntityPlaceableFlamingRain(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    public void onUpdate() {
        if (width != getRadius() * 2) {
            setSize(getRadius() * 2, 0);
        }
        super.onUpdate();
    }

    @Override
    public float getEyeHeight() {
        return this.height * 0.5F;
    }

    @Override
    protected void setSize(float width, float height) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double w = width / 2D;
        double h = height;
        this.width = width;
        this.height = height;
        setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX - w, axisalignedbb.minY - h, axisalignedbb.minZ - w, axisalignedbb.minX + w, axisalignedbb.minY, axisalignedbb.minZ + w));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - f1, z - (double) f, x + (double) f, y, z + (double) f));
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }
}
