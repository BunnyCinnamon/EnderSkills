package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityFinalFlash extends Entity {

    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityFinalFlash.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Float> LIFE_TIME = EntityDataManager.createKey(EntityFinalFlash.class, DataSerializers.FLOAT);
    public static final DataParameter<BlockPos> SUCK_DICK = EntityDataManager.createKey(EntityFinalFlash.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<Float> RADIUS = EntityDataManager.createKey(EntityFinalFlash.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> RANGE = EntityDataManager.createKey(EntityFinalFlash.class, DataSerializers.FLOAT);
    public static final DataParameter<Integer> SEED = EntityDataManager.createKey(EntityFinalFlash.class, DataSerializers.VARINT);
    public static final int MIN_TIME = 10;
    public static double collapse = 0.95D;
    public ArrayList<UUID> entities = Lists.newArrayList();
    public ArrayList<Float> pointsWidth = Lists.newArrayList();
    public ArrayList<Vec3d> points = Lists.newArrayList();
    public int tick;
    public int tickDelay;

    public EntityFinalFlash(World worldIn) {
        super(worldIn);
        this.noClip = true;
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        setSize(0F, 0F);
    }

    public EntityFinalFlash(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, float lifeTime) {
        this(worldIn);
        if (owner != null) {
            this.rotationPitch = owner.rotationPitch;
            this.rotationYaw = owner.rotationYaw;
        }
        setNoGravity(false);
        setData(skillData);
        setLifeTime(lifeTime);
        if (owner != null) {
            this.dataManager.set(SUCK_DICK, new BlockPos(owner.getPositionEyes(1.0F)));
        }
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(LIFE_TIME, 0F);
        this.dataManager.register(RANGE, 0F);
        this.dataManager.register(RADIUS, 0F);
        this.dataManager.register(SUCK_DICK, new BlockPos(0, 0, 0));
        this.dataManager.register(SEED, this.world.rand.nextInt());
    }

    private void setupShape(Random rr) {
        points.clear();
        pointsWidth.clear();
        int steps = 12;
        float girth = getRadius();
        Vec3d left = new Vec3d(getRange() / (steps + 1), 0, 0);
        Vec3d right = left.scale(-0.3);
        Vec3d lr = new Vec3d(0, 0, 0);
        Vec3d ll = new Vec3d(0, 0, 0);
        float dec = girth / steps;
        for (int a = 0; a < steps; ++a) {
            girth -= dec;
            lr = lr.add(right);
            points.add(new Vec3d(lr.x, lr.y, lr.z));
            pointsWidth.add((float) (girth * ((1F - (float) a / (float) steps))));
            ll = ll.add(left);
            points.add(0, new Vec3d(ll.x, ll.y, ll.z));
            pointsWidth.add(0, (float) (getRadius() * (a + 1 == steps ? 0 : 1)));
        }
        lr = lr.add(right);
        points.add(new Vec3d(lr.x, lr.y, lr.z));
        pointsWidth.add(0F);
        ll = ll.add(left);
        points.add(0, new Vec3d(ll.x, ll.y, ll.z));
        pointsWidth.add(0, 0F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ignoreFrustumCheck = true;
        noClip = true;
        if (world.isRemote && getRadius() != 0 && points.isEmpty()) {
            this.setupShape(new Random(this.getSeed()));
        }
        if (getRadius() > 0) {
            if (tickDelay == 0) {
                world.playSound(posX, posY, posZ, ModSounds.FINAL_FLASH_CAST, SoundCategory.HOSTILE, 8.0F, 1.0F, true);
            }
            if (!world.isRemote) {
                SkillData data = getData();
                EntityLivingBase owner = SkillHelper.getOwner(data);
                double radius = getRadius();
                float scale = (float) getScale(tick);
                float size = (float) (scale * radius);
                if (tick > this.getLifeTime()) {
                    setDead();
                }
                if (tickDelay > getData().nbt.getInteger("delay")) {
                    if (tick == 0) {
                        world.playSound(posX, posY, posZ, ModSounds.FINAL_FLASH_RELEASE, SoundCategory.HOSTILE, 8.0F, 1.0F, true);
                    }
                    List<EntityLivingBase> found = RayTraceHelper.findInRangeSize(this, getRange(), size, owner);
                    for (EntityLivingBase entity : found) {
                        ModAbilities.FINAL_FLASH.apply(entity, getData());
                        ModEffects.SLOWED.set(entity, getData(), 0.01D);
                        if (!entities.contains(entity.getPersistentID()) && entities.add(entity.getPersistentID())) {
                            if (SkillHelper.isActive(entity, ModEffects.GLOWING)) {
                                ModEffects.GLOWING.activate(entity, data);
                            } else {
                                ModEffects.GLOWING.set(entity, data);
                            }
                        }
                    }
                    AxisAlignedBB bb = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
                    setEntityBoundingBox(bb);
                } else {
                    float v = 1F;
                    AxisAlignedBB bb = new AxisAlignedBB(posX - v, posY - v, posZ - v, posX + v, posY + v, posZ + v);
                    setEntityBoundingBox(bb);
                }
            }
        }
        if (tickDelay > getData().nbt.getInteger("delay")) {
            tick++;
        } else {
            tickDelay++;
        }
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
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPosition()).grow(getRange());
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

    public void setSeed(int seed) {
        this.dataManager.set(SEED, seed);
    }

    public int getSeed() {
        return this.dataManager.get(SEED);
    }

    public void setLifeTime(float age) {
        dataManager.set(LIFE_TIME, age);
    }

    public float getLifeTime() {
        return dataManager.get(LIFE_TIME);
    }

    public void setRadius(double size) {
        this.dataManager.set(RADIUS, (float) size);
    }

    public void setRange(double size) {
        this.dataManager.set(RANGE, (float) size);
    }

    public float getRange() {
        return this.dataManager.get(RANGE);
    }

    public float getRadius() {
        return this.dataManager.get(RADIUS);
    }

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setData(new SkillData(compound.getCompoundTag("data")));
        setLifeTime(compound.getFloat("lifeTime"));
        setRadius(compound.getFloat("radius"));
        setRadius(compound.getFloat("size"));
        this.dataManager.set(SUCK_DICK, NBTHelper.getBlockPos(compound, "aaa"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("data", getData().serializeNBT());
        compound.setFloat("lifeTime", getLifeTime());
        compound.setFloat("radius", getRadius());
        compound.setFloat("size", getRange());
        NBTHelper.setBlockPos(compound, "aaa", this.dataManager.get(SUCK_DICK));
    }

    public double getScale(float age) {
        double life = age / this.getLifeTime();

        double curve;
        if (life < collapse) {
            curve = 0.005 + ease(1 - ((collapse - life) / collapse)) * 0.995;
        } else {
            curve = ease(1 - ((life - collapse) / (1 - collapse)));
        }
        return curve;
    }

    private static double ease(double in) {
        double t = in - 1;
        return Math.sqrt(1 - t * t);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }
}
