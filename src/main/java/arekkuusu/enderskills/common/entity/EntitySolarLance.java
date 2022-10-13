package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.sounds.SolarLanceSound;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("Guava")
public class EntitySolarLance extends Entity {

    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntitySolarLance.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Float> LIFE_TIME = EntityDataManager.createKey(EntitySolarLance.class, DataSerializers.FLOAT);
    public static final DataParameter<BlockPos> SUCK_DICK = EntityDataManager.createKey(EntitySolarLance.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntitySolarLance.class, DataSerializers.FLOAT);
    public static final DataParameter<Integer> SEED = EntityDataManager.createKey(EntitySolarLance.class, DataSerializers.VARINT);
    public static final int MIN_TIME = 10;
    public static double collapse = 0.95D;
    public ArrayList<UUID> entities = Lists.newArrayList();
    public ArrayList<Float> pointsWidth = Lists.newArrayList();
    public ArrayList<Vec3d> points = Lists.newArrayList();
    public int penesMaximus;
    public int penes;
    public int tick;

    public EntitySolarLance(World worldIn) {
        super(worldIn);
        this.noClip = true;
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        setSize(0F, 0F);
    }

    public EntitySolarLance(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, float lifeTime) {
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
        this.dataManager.register(SIZE, 0F);
        this.dataManager.register(SUCK_DICK, new BlockPos(0, 0, 0));
        this.dataManager.register(SEED, this.world.rand.nextInt());
    }

    private void setupShape(Random rr) {
        points.clear();
        pointsWidth.clear();
        int steps = 8;
        float girth = 0.8F;
        Vec3d right = new Vec3d(15D / (steps + 1), 0, 0);
        Vec3d left = right.scale(-1);
        Vec3d lr = new Vec3d(0, 0, 0);
        Vec3d ll = new Vec3d(0, 0, 0);
        float dec = girth / steps;
        for (int a = 0; a < steps; ++a) {
            girth -= dec;
            lr = lr.add(right);
            points.add(new Vec3d(lr.x, lr.y, lr.z));
            pointsWidth.add((float) (girth * Math.pow((1F - (float) a / (float) steps), 3)));
            ll = ll.add(left);
            points.add(0, new Vec3d(ll.x, ll.y, ll.z));
            pointsWidth.add(0, (float) (girth * Math.pow((1F - (float) a / (float) steps), 3)));
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
        ignoreFrustumCheck = true;
        noClip = true;
        if (world.isRemote && !isDead) {
            makeSound();
        }
        super.onUpdate();
        rotateTowardsMovement(this, 1F);
        if (world.isRemote && getRadius() != 0 && points.isEmpty()) {
            this.setupShape(new Random(this.getSeed()));
        }
        if (getRadius() > 0) {
            if (tick == 0) {
                world.playSound(posX, posY, posZ, ModSounds.SOLAR_LANCE_CAST, SoundCategory.PLAYERS, 8.0F, 1.0F, true);
            }
            if (!world.isRemote) {
                SkillData data = getData();
                EntityLivingBase owner = SkillHelper.getOwner(data);
                double radius = getRadius();
                float scale = (float) getScale(1F);
                BlockPos blockPos = this.dataManager.get(SUCK_DICK);
                if (!(getPosition().getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) < this.getLifeTime())) {
                    setDead();
                }
                RayTraceResult raytraceresult = RayTraceHelper.forwardsRaycastWeirdPogchamp(this, radius, this.tick >= 25, owner);
                if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.ENTITY
                        && raytraceresult.entityHit instanceof EntityLivingBase
                        && !entities.contains(raytraceresult.entityHit.getPersistentID())) {
                    entities.add(raytraceresult.entityHit.getPersistentID());
                    ModAbilities.SOLAR_LANCE.apply((EntityLivingBase) raytraceresult.entityHit, getData());
                    if (SkillHelper.isActive(raytraceresult.entityHit, ModEffects.GLOWING)) {
                        ModEffects.GLOWING.activate((EntityLivingBase) raytraceresult.entityHit, data);
                    } else {
                        //ModEffects.GLOWING.set((EntityLivingBase) raytraceresult.entityHit, data);
                    }
                    penes++;
                    if (penes >= penesMaximus) {
                        setDead();
                    }
                }
                float size = scale * 1F;
                AxisAlignedBB bb = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
                setEntityBoundingBox(bb);
            }
        }
        if (world.isRemote) {
            Vec3d pos = getPositionVector();
            double particlespeed = 0.05;
            Vec3d particlePos = new Vec3d(0, 0, getRadius());
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
        tick++;
        if (!world.isRemote) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            markVelocityChanged();
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

    @SideOnly(Side.CLIENT)
    public void makeSound() {
        if (firstUpdate) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new SolarLanceSound(this));
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
        this.dataManager.set(SIZE, (float) size);
    }

    public float getRadius() {
        return this.dataManager.get(SIZE);
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
        penes = compound.getInteger("penes");
        penesMaximus = compound.getInteger("penesMaximus");
        this.dataManager.set(SUCK_DICK, NBTHelper.getBlockPos(compound, "aaa"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("data", getData().serializeNBT());
        compound.setFloat("lifeTime", getLifeTime());
        compound.setFloat("radius", getRadius());
        NBTHelper.setBlockPos(compound, "aaa", this.dataManager.get(SUCK_DICK));
        compound.setInteger("penesMaximus", penesMaximus);
        compound.setInteger("penes", penes);
    }

    public double getScale(float age) {
        BlockPos blockPoosu = this.dataManager.get(SUCK_DICK);
        double life = getPosition().getDistance(blockPoosu.getX(), blockPoosu.getY(), blockPoosu.getZ()) / this.getLifeTime();

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
