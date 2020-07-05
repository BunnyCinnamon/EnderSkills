package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.common.entity.data.ExtendedData;
import arekkuusu.enderskills.common.entity.data.ListBlockStateExtendedData;
import arekkuusu.enderskills.common.entity.data.WallSegmentBehaviorExtendedData;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityWallSegment extends Entity {

    public static final DataParameter<WallSegmentBehaviorExtendedData> SYNC_BEHAVIOR = EntityDataManager.createKey(EntityWallSegment.class, WallSegmentBehaviorExtendedData.SERIALIZER);
    public static final DataParameter<ListBlockStateExtendedData> SYNC_BLOCKS_DATA = EntityDataManager.createKey(EntityWallSegment.class, ListBlockStateExtendedData.SERIALIZER);
    public static final DataParameter<Optional<UUID>> SYNC_WALL = EntityDataManager.createKey(EntityWallSegment.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Integer> SYNC_DURATION = EntityDataManager.createKey(EntityWallSegment.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> SYNC_SIZE = EntityDataManager.createKey(EntityWallSegment.class, DataSerializers.VARINT);

    public EntityWallSegment(World world) {
        super(world);
        setSize(1F, 1F);
    }

    @Override
    public void onUpdate() {
        if (firstUpdate) {
            setSize(1F, getSize());
        }
        super.onUpdate();
        ignoreFrustumCheck = true;
        noClip = true;
        if (!world.isRemote) {
            if (getWall() == null || ticksExisted > getDuration() + 10) {
                this.setDead();
            }
            WallSegmentBehaviorExtendedData next = getBehavior().update(this);
            if (getBehavior() != next) setBehavior(next);
            move(MoverType.SELF, 0, motionY, 0);
            markVelocityChanged();
        }
        if (getBehavior().getClass() == WallSegmentBehaviorExtendedData.Rising.class) {
            collideWithNearbyEntities();
        }
    }

    public void collideWithNearbyEntities() {
        EntityWall wall = getWall();
        if (wall != null) {
            AxisAlignedBB bb = getEntityBoundingBox().expand(0, motionY, 0);
            List<Entity> list = this.world.getEntitiesInAABBexcluding(this, bb, EntitySelectors.NOT_SPECTATING);
            for (Entity entity : list) {
                Vec3d pos = entity.getPositionVector();
                if (canCollideWith(entity) && pos.y > posY) {
                    double yOffset = posY + motionY + getSize();
                    entity.setPositionAndUpdate(pos.x, yOffset, pos.z);
                    if (entity.motionY < 0) entity.motionY = 0;
                    double launch = (1D + (wall.getLaunch() / 20D)) / 5D;
                    entity.motionY += launch;
                    entity.velocityChanged = true;
                }
            }
        }
    }

    public boolean canCollideWith(Entity entity) {
        return !(entity instanceof EntityWall) && !(entity instanceof EntityWallSegment);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public float getCollisionBorderSize() {
        return 0F;
    }

    @Override
    public void applyEntityCollision(Entity entity) {
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public void setDead() {
        super.setDead();
        if (getWall() != null) getWall().setDead();
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public float getEyeHeight() {
        return height / 2;
    }

    @SuppressWarnings("Guava")
    public void setWall(EntityWall wall) {
        this.dataManager.set(SYNC_WALL, Optional.of(wall.getUniqueID()));
    }

    @Nullable
    public EntityWall getWall() {
        UUID uuid = this.dataManager.get(SYNC_WALL).orNull();
        if (uuid != null) {
            for (Entity entity : getEntityWorld().loadedEntityList) {
                if (entity.getUniqueID().equals(uuid) && entity instanceof EntityWall) {
                    return (EntityWall) entity;
                }
            }
        }
        return null;
    }

    public IBlockState[] getBlocks() {
        return dataManager.get(SYNC_BLOCKS_DATA).states;
    }

    public void setBlocks(IBlockState[] states) {
        ListBlockStateExtendedData data = dataManager.get(SYNC_BLOCKS_DATA);
        data.states = states;
        dataManager.set(SYNC_BLOCKS_DATA, data);
    }

    @Nullable //Suck my balls
    public WallSegmentBehaviorExtendedData getBehavior() {
        return dataManager.get(SYNC_BEHAVIOR);
    }

    public void setBehavior(WallSegmentBehaviorExtendedData behavior) {
        dataManager.set(SYNC_BEHAVIOR, behavior);
    }

    public int getDuration() {
        return this.dataManager.get(SYNC_DURATION);
    }

    public void setDuration(int duration) {
        this.dataManager.set(SYNC_DURATION, duration);
    }

    public int getSize() {
        return this.dataManager.get(SYNC_SIZE);
    }

    public void setSize(int size) {
        this.dataManager.set(SYNC_SIZE, size);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(SYNC_SIZE, 0);
        this.dataManager.register(SYNC_DURATION, 0);
        this.dataManager.register(SYNC_WALL, Optional.absent());
        this.dataManager.register(SYNC_BLOCKS_DATA, new ListBlockStateExtendedData());
        this.dataManager.register(SYNC_BEHAVIOR, new WallSegmentBehaviorExtendedData.Rising());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setSize(compound.getInteger("size"));
        setDuration(compound.getInteger("duration"));
        if (compound.hasKey("uuid")) {
            this.dataManager.set(SYNC_WALL, Optional.fromNullable(compound.getUniqueId("uuid")));
        } else {
            this.dataManager.set(SYNC_WALL, Optional.absent());
        }
        NBTTagList stateList = compound.getTagList("states", Constants.NBT.TAG_COMPOUND);
        IBlockState[] states = new IBlockState[stateList.tagCount()];
        for (int i = 0; i < states.length; i++) {
            states[i] = Block.getStateById(stateList.getCompoundTagAt(i).getInteger("state"));
        }
        setBlocks(states);
        setBehavior((WallSegmentBehaviorExtendedData) ExtendedData.lookup(compound.getInteger("behavior")));
        ListBlockStateExtendedData data = new ListBlockStateExtendedData();
        data.deserializeNBT(compound.getCompoundTag("SYNC_BLOCKS_DATA"));
        this.dataManager.set(SYNC_BLOCKS_DATA, data);
        if(compound.hasKey("SYNC_WALL")) {
            this.dataManager.set(SYNC_WALL, Optional.fromNullable(compound.getUniqueId("SYNC_WALL")));
        }
        setDuration(compound.getInteger("SYNC_DURATION"));
        setSize(compound.getInteger("SYNC_SIZE"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("size", getSize());
        compound.setInteger("duration", getDuration());
        if (dataManager.get(SYNC_WALL).isPresent()) {
            compound.setUniqueId("uuid", dataManager.get(SYNC_WALL).get());
        }
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getBlocks().length; i++) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("state", Block.getStateId(getBlocks()[i]));
            list.appendTag(nbt);
        }
        compound.setTag("states", list);
        compound.setInteger("behavior", getBehavior().getId());
        compound.setTag("SYNC_BLOCKS_DATA", this.dataManager.get(SYNC_BLOCKS_DATA).serializeNBT());
        if(this.dataManager.get(SYNC_WALL).isPresent()) {
            compound.setUniqueId("SYNC_WALL", this.dataManager.get(SYNC_WALL).get());
        }
        compound.setInteger("SYNC_DURATION", this.dataManager.get(SYNC_DURATION));
        compound.setInteger("SYNC_SIZE", this.dataManager.get(SYNC_SIZE));
    }
}
