package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.client.sounds.PortalActiveSound;
import arekkuusu.enderskills.client.sounds.PortalInactiveSound;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("Guava")
public class EntityPortal extends Entity {

    public static final DataParameter<Optional<UUID>> TARGET_ID = EntityDataManager.createKey(EntityPortal.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Integer> STATUS = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> LIFE_TIME = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> SEED = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    public ArrayList<Float> pointsWidth = Lists.newArrayList();
    public ArrayList<Vec3d> points = Lists.newArrayList();
    public Set<Entity> teleported = Collections.newSetFromMap(new WeakHashMap<>());
    public int tick;

    @SideOnly(Side.CLIENT)
    public boolean open;
    public int openAnimationTimer = -1;

    public EntityPortal(World world) {
        super(world);
        this.setSize(1.5F, 2F);
        this.setupShape(new Random(this.getSeed()));
    }

    public EntityPortal(World worldIn, int lifeTime) {
        this(worldIn);
        setLifeTime(lifeTime);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(TARGET_ID, Optional.absent());
        this.dataManager.register(STATUS, 0);
        this.dataManager.register(LIFE_TIME, 0);
        this.dataManager.register(SEED, this.world.rand.nextInt());
    }

    private void setupShape(Random rr) {
        points.clear();
        pointsWidth.clear();
        int steps = 6;
        float girth = width;
        double angle = 0.15D;
        Vec3d right = new Vec3d(0, height / (steps + 1), 0);
        right = right.rotateYaw((float) (rr.nextGaussian() * 360F));
        right = right.rotatePitch((float) (rr.nextGaussian() * 180F));
        Vec3d left = right.scale(-1);
        Vec3d lr = new Vec3d(0, 0, 0);
        Vec3d ll = new Vec3d(0, 0, 0);
        float dec = girth / steps;
        for (int a = 0; a < steps; ++a) {
            girth -= dec;
            right = right.rotatePitch((float) (rr.nextGaussian() * angle));
            right = right.rotateYaw((float) (rr.nextGaussian() * angle));
            lr = lr.add(right);
            points.add(new Vec3d(lr.x, lr.y, lr.z));
            pointsWidth.add(girth);
            left = left.rotatePitch((float) (rr.nextGaussian() * angle));
            left = left.rotateYaw((float) (rr.nextGaussian() * angle));
            ll = ll.add(left);
            points.add(0, new Vec3d(ll.x, ll.y, ll.z));
            pointsWidth.add(0, girth);
        }
        lr = lr.add(right);
        points.add(new Vec3d(lr.x, lr.y, lr.z));
        pointsWidth.add(0.0F);
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
        if (!world.isRemote) {
            if (this.getLifeTime() > this.tick) {
                EntityPortal target = getTarget();
                if (target != null) {
                    if (tick == 0) {
                        teleported.addAll(world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox()));
                    }
                    List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox());
                    teleported.removeIf(e -> !entities.contains(e));
                    for (Entity entity : entities) {
                        if (!teleported.contains(entity)) {
                            teleported.remove(entity);
                            entity.setPositionAndUpdate(target.posX, target.posY, target.posZ);
                            target.teleported.add(entity);
                            if (world instanceof WorldServer) {
                                ((WorldServer) entity.world).playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ, ModSounds.PORTAL_OPEN, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
                            }
                        }
                    }
                }
            } else {
                EntityPortal target = getTarget();
                if (target == null || target.tick >= target.getLifeTime()) {
                    setDead();
                }
            }
        } else {
            if (openAnimationTimer == -1 && isOpen()) {
                openAnimationTimer = 10;
            } else if (openAnimationTimer > 0) {
                --openAnimationTimer;
            }
        }
        this.tick++;
    }

    @SideOnly(Side.CLIENT)
    public void makeSound() {
        if (firstUpdate) {
            world.playSound(null, posX, posY, posZ, ModSounds.PORTAL_PLACE, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
            if (!isOpen()) {
                Minecraft.getMinecraft().getSoundHandler().playSound(new PortalInactiveSound(this));
            }
        }
        if (isOpen() && !open) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new PortalActiveSound(this));
            open = true;
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
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height / 2F;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - f1, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
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

    public boolean isOpen() {
        return this.dataManager.get(TARGET_ID).isPresent();
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

    public void setTarget(@Nullable EntityPortal owner) {
        this.dataManager.set(TARGET_ID, owner != null ? Optional.of(owner.getUniqueID()) : Optional.absent());
    }

    @Nullable
    public EntityPortal getTarget() {
        EntityPortal owner = null;
        if (this.dataManager.get(TARGET_ID).isPresent()) {
            UUID uuid = this.dataManager.get(TARGET_ID).get();
            Entity entity = ((WorldServer) this.world).getEntityFromUuid(uuid);
            if (entity instanceof EntityPortal) {
                owner = (EntityPortal) entity;
            }
        }

        return owner;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        String uuidString;
        if (compound.hasKey("TargetUUID", 8)) {
            uuidString = compound.getString("TargetUUID");
        } else {
            String ownerString = compound.getString("Owner");
            uuidString = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), ownerString);
        }
        if (!uuidString.isEmpty()) {
            this.dataManager.set(TARGET_ID, Optional.of(UUID.fromString(uuidString)));
        }
        setLifeTime(compound.getInteger("lifeTime"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (!this.dataManager.get(TARGET_ID).isPresent()) {
            compound.setString("TargetUUID", "");
        } else {
            compound.setString("TargetUUID", this.dataManager.get(TARGET_ID).get().toString());
        }
        compound.setInteger("lifeTime", getLifeTime());
    }
}
