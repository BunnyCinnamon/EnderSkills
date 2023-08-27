package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.data.ListUUIDExtendedData;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.entity.data.WallSegmentBehaviorExtendedData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.UUID;

public class EntityWall extends Entity {

    public static final DataParameter<ListUUIDExtendedData> SYNC_UUID_DATA = EntityDataManager.createKey(EntityWall.class, ListUUIDExtendedData.SERIALIZER);
    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityWall.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Float> SYNC_LAUNCH = EntityDataManager.createKey(EntityWall.class, DataSerializers.FLOAT);
    public boolean goingDown;

    public EntityWall(World world) {
        super(world);
        setSize(0, 0);
    }

    public EntityWall(World world, SkillData skillData) {
        super(world);
        setData(skillData);
        setSize(0, 0);
    }

    @Override
    public void onUpdate() {
        if (isDead) return;
        if (world.isRemote && !goingDown && getSegments().length > 0 && getSegments()[0] != null && getSegments()[0].getBehavior() != null && getSegments()[0].getBehavior().getClass() == WallSegmentBehaviorExtendedData.Drop.class) {
            if (getData().skill == ModAbilities.WALL) {
                world.playSound(posX, posY, posZ, ModSounds.WALL_DOWN, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F, false);
            } else {
                world.playSound(posX, posY, posZ, ModSounds.DOME_DOWN, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F, false);
            }
            goingDown = true;
        }
        super.onUpdate();
        ignoreFrustumCheck = true;
        noClip = true;
        motionX = 0;
        motionY = 0;
        motionZ = 0;

        if (getSegments().length == 0) setDead();
    }

    public void create(BlockPos pos, EnumFacing cardinal, int width, int height, int duration) {
        loop:
        for (int i = 0; i < width; i++) {
            int mod = -(width / 2) + i;
            int x = pos.getX() + (cardinal == EnumFacing.NORTH || cardinal == EnumFacing.SOUTH ? mod : 0);
            int y = pos.getY() - height;
            int z = pos.getZ() + (cardinal == EnumFacing.EAST || cardinal == EnumFacing.WEST ? mod : 0);

            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(x, y + height - 1, z);
            if (!isSolid(world, mPos)) {
                for (int j = 0; j < 4; j++) {
                    if (j == 3) continue loop;
                    mPos.move(EnumFacing.DOWN);
                    y--;
                    if (isSolid(world, mPos)) {
                        break;
                    }
                }
            } else if (isSolid(world, mPos.up())) {
                for (int j = 0; j < 4; j++) {
                    if (j == 3) continue loop;
                    mPos.move(EnumFacing.UP);
                    y++;
                    if (!isSolid(world, mPos.up())) {
                        break;
                    }
                }
            }

            if (!isOverlappingWall(world, mPos, 1)) {
                boolean foundLand = false;
                int foundLandIndex = 0;
                IBlockState[] states = new IBlockState[height];
                for (int j = height - 1; j >= 0; j--) {
                    IBlockState stateSeg = world.getBlockState(mPos);
                    boolean solid = isSolid(world, mPos);
                    if (!solid) {
                        stateSeg = !foundLand ? Blocks.AIR.getDefaultState() : states[j + 1];
                    } else if (!foundLand) {
                        foundLand = true;
                        foundLandIndex = height - (j + 1);
                    }

                    states[j] = stateSeg;
                    mPos.move(EnumFacing.DOWN);
                }
                if (Arrays.stream(states).allMatch(s -> s == Blocks.AIR.getDefaultState())) return;
                EntityWallSegment segment = new EntityWallSegment(world);
                segment.setBlocks(states);
                segment.setSize(height - foundLandIndex);
                segment.setPosition(x + 0.5D, y, z + 0.5D);
                segment.setDuration(duration);
                segment.setWall(this);
                world.spawnEntity(segment);
                addSegment(segment);
            }
        }
    }

    public boolean isOverlappingWall(World world, BlockPos pos, int size) {
        AxisAlignedBB bb = new AxisAlignedBB(pos).grow(0, size, 0);
        return !world.getEntitiesWithinAABB(EntityWallSegment.class, bb).isEmpty();
    }

    public EntityWallSegment[] getSegments() {
        UUID[] uuids = this.dataManager.get(SYNC_UUID_DATA).uuids;
        if (uuids == null) return new EntityWallSegment[0];
        EntityWallSegment[] segments = new EntityWallSegment[uuids.length];
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = uuids[i];

            for (Entity entity : getEntityWorld().loadedEntityList) {
                if (entity.getUniqueID().equals(uuid) && entity instanceof EntityWallSegment) {
                    segments[i] = (EntityWallSegment) entity;
                    break;
                }
            }
        }
        return segments;
    }

    public boolean isSolid(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
    }

    public void addSegment(EntityWallSegment segment) {
        ListUUIDExtendedData data = this.dataManager.get(SYNC_UUID_DATA);
        data.uuids = Arrays.copyOf(data.uuids, data.uuids.length + 1);
        data.uuids[data.uuids.length - 1] = segment.getUniqueID();
        this.dataManager.set(SYNC_UUID_DATA, data);
    }

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    public float getLaunch() {
        return this.dataManager.get(SYNC_LAUNCH);
    }

    public void setLaunch(float launch) {
        this.dataManager.set(SYNC_LAUNCH, launch);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
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
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(SYNC_UUID_DATA, new ListUUIDExtendedData());
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(SYNC_LAUNCH, 0F);
    }

    public void spawnEntity() {
        this.world.spawnEntity(this);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        ListUUIDExtendedData data = new ListUUIDExtendedData();
        data.deserializeNBT(compound.getCompoundTag("SYNC_UUID_DATA"));
        this.dataManager.set(SYNC_UUID_DATA, data);
        SkillExtendedData data1 = new SkillExtendedData();
        data1.deserializeNBT(compound.getCompoundTag("DATA"));
        this.dataManager.set(DATA, data1);
        this.dataManager.set(SYNC_LAUNCH, compound.getFloat("SYNC_LAUNCH"));
        this.goingDown = compound.getBoolean("goingDown");
        this.ticksExisted = compound.getInteger("ticksExisted");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("SYNC_UUID_DATA", this.dataManager.get(SYNC_UUID_DATA).serializeNBT());
        compound.setTag("DATA", this.dataManager.get(DATA).serializeNBT());
        compound.setFloat("SYNC_LAUNCH", this.dataManager.get(SYNC_LAUNCH));
        compound.setInteger("ticksExisted", this.ticksExisted);
    }
}
