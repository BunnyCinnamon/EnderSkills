package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.client.sounds.BlackHoleSound;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.entity.throwable.MotionHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("Guava")
public class EntityBlackHole extends Entity {

    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityBlackHole.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Integer> LIFE_TIME = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.VARINT);
    public static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.FLOAT);
    public static final DataParameter<Integer> SEED = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.VARINT);
    public static final int MIN_TIME = 10;
    public static double collapse = 0.95D;
    public ArrayList<Float> pointsWidth = Lists.newArrayList();
    public ArrayList<Vec3d> points = Lists.newArrayList();
    public int tick;

    public EntityBlackHole(World worldIn) {
        super(worldIn);
        this.noClip = true;
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        setSize(0F, 0F);
    }

    public EntityBlackHole(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        this(worldIn);
        if (owner != null) {
            this.rotationPitch = owner.rotationPitch;
            this.rotationYaw = owner.rotationYaw;
        }
        setData(skillData);
        setLifeTime(lifeTime);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(LIFE_TIME, 0);
        this.dataManager.register(SIZE, 0F);
        this.dataManager.register(SEED, this.world.rand.nextInt());
    }

    private void setupShape(Random rr) {
        points.clear();
        pointsWidth.clear();
        int steps = 9;
        float girth = getRadius() * 1.2F;
        Vec3d right = new Vec3d(0, getRadius() * 3 / (steps + 1), 0);
        right = right.rotatePitch((float) (90F * Math.PI / 180F));
        right = right.rotateYaw((float) (rr.nextGaussian() * 360F));
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
        if (world.isRemote && !isDead) {
            makeSound();
        }
        super.onUpdate();
        if (world.isRemote && getRadius() != 0 && points.isEmpty()) {
            this.setupShape(new Random(this.getSeed()));
        }
        if (getRadius() > 0) {
            if (getLifeTime() > this.tick) {
                if (tick == 0) {
                    world.playSound(posX, posY, posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 8.0F, 1.0F, true);
                }
                SkillData data = getData();
                EntityLivingBase owner = SkillHelper.getOwner(data);
                double radius = getRadius();
                double suckRange = radius * 2;
                AxisAlignedBB suckzone = new AxisAlignedBB(this.posX - suckRange, this.posY - suckRange, this.posZ - suckRange, this.posX + suckRange, this.posY + suckRange, this.posZ + suckRange);
                List<EntityLivingBase> succ = getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, suckzone, TeamHelper.SELECTOR_ENEMY.apply(owner));
                if (!succ.isEmpty()) {
                    for (EntityLivingBase entity : succ) {
                        if ((!world.isRemote || entity instanceof EntityPlayer)) {
                            double dx = posX - entity.posX;
                            double dy = posY - entity.posY;
                            double dz = posZ - entity.posZ;

                            double lensquared = dx * dx + dy * dy + dz * dz;
                            double len = Math.sqrt(lensquared);
                            double lenn = len / suckRange;

                            if (len <= suckRange) {
                                double strength = (1 - lenn) * (1 - lenn);
                                double power = 0.075 * radius;

                                double motionX = entity.motionX + (dx / len) * strength * power;
                                double motionY = entity.motionY + (dy / len) * strength * power;
                                double motionZ = entity.motionZ + (dz / len) * strength * power;
                                if (Double.isFinite(motionX) && Double.isFinite(motionY) && Double.isFinite(motionZ)) {
                                    entity.motionX = motionX;
                                    entity.motionY = motionY;
                                    entity.motionZ = motionZ;
                                }
                            }
                        }
                        if (!world.isRemote) {
                            if (!SkillHelper.isActive(entity, ModEffects.BLINDED)) {
                                ModEffects.BLINDED.set(entity, getData());
                            }
                            ModEffects.VOIDED.set(entity, getData());
                            ModEffects.SLOWED.set(entity, getData(), 0.6D);
                        }
                    }
                }
                float size = (float) getScale(tick) * getRadius();
                AxisAlignedBB bb = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
                setEntityBoundingBox(bb);
            } else {
                this.world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
                if (!world.isRemote) {
                    EntityLivingBase owner = SkillHelper.getOwner(getData());
                    List<EntityLivingBase> fucc = getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
                    for (EntityLivingBase entity : fucc) {
                        MotionHelper.pushAround(this, entity, 2);
                        ModAbilities.BLACK_HOLE.apply(entity, getData().copy());
                    }
                }
                setDead();
            }
        }
        if (world.isRemote) {
            Vec3d pos = getPositionVector();
            double particlespeed = 4.5;
            for (int i = 0; i < 50; i++) {
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

                world.spawnParticle(EnumParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
            }
        }
        tick++;
    }

    @SideOnly(Side.CLIENT)
    public void makeSound() {
        if (firstUpdate) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new BlackHoleSound(this));
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

    public void setLifeTime(int age) {
        dataManager.set(LIFE_TIME, age);
    }

    public int getLifeTime() {
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
        setLifeTime(compound.getInteger("lifeTime"));
        setRadius(compound.getFloat("radius"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("data", getData().serializeNBT());
        compound.setInteger("lifeTime", getLifeTime());
        compound.setFloat("radius", getRadius());
    }

    public double getScale(int age) {
        double life = (double) age / (double) getLifeTime();

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
