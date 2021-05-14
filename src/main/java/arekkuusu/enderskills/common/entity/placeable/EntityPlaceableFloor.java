package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.data.ListBlockPosExtendedData;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityPlaceableFloor extends EntityPlaceableData {

    public static final DataParameter<ListBlockPosExtendedData> SYNC_BLOCKS_DATA = EntityDataManager.createKey(EntityPlaceableFloor.class, ListBlockPosExtendedData.SERIALIZER);
    public double cursor = 0;

    public EntityPlaceableFloor(World world) {
        super(world);
        setSize(0F, 6F);
    }

    public EntityPlaceableFloor(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
        setSize(0F, 6F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SYNC_BLOCKS_DATA, new ListBlockPosExtendedData());
    }

    @Override
    public void onUpdate() {
        if (tick < MIN_TIME) {
            updateSpread();
        }
        super.onUpdate();
    }

    public void updateSpread() {
        BlockPos[][] positions = this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
        double perTick = (double) (positions.length) / (double) (MIN_TIME);
        cursor += perTick;
    }

    public void spreadOnTerrain() {
        ListBlockPosExtendedData data = new ListBlockPosExtendedData();
        BlockPos[][] positions = this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
        int radius = (int) Math.floor(getRadius());
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> queue = new HashSet<>();
        BlockPos origin = getPosition().down(); //Down into the ground if any
        BlockPos original = getValid(origin); //Look for the ground
        if (original != null) {
            positions = Arrays.copyOf(positions, positions.length + 1);
            positions[0] = new BlockPos[]{original};
            visited.add(original); //We know our original position is valid
            addNext(queue, original); //Add next on queue
            int i = 1;
            while (true) {
                positions = Arrays.copyOf(positions, positions.length + 1);
                positions[i] = new BlockPos[0];
                Set<BlockPos> temp = new HashSet<>();
                int j = 0;
                for (BlockPos pos : queue) {
                    BlockPos validated = getValid(pos);
                    if (validated != null && isWithingRadius(original, validated, radius)) {
                        if (visited.add(validated)) {
                            positions[i] = Arrays.copyOf(positions[i], positions[i].length + 1);
                            positions[i][j] = validated;
                            addNext(temp, validated);
                            j++;
                        }
                    }
                }
                if (temp.isEmpty()) break;
                queue = temp;
                i++;
            }
        }
        data.posArray = positions;
        this.dataManager.set(SYNC_BLOCKS_DATA, data);
    }

    @Nullable
    public BlockPos getValid(BlockPos pos) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos);

        if (!isSolid(mPos)) {
            for (int j = 0; ; j++) {
                if (j == 1) return null;
                mPos.move(EnumFacing.DOWN);
                if (isSolid(mPos)) {
                    return mPos.toImmutable();
                }
            }
        } else if (isSolid(mPos.up())) {
            for (int j = 0; ; j++) {
                if (j == 1) return null;
                mPos.move(EnumFacing.UP);
                if (!isSolid(mPos.up())) {
                    return mPos.toImmutable();
                }
            }
        }
        return mPos.toImmutable();
    }

    public boolean isWithingRadius(BlockPos origin, BlockPos pos, int distance) {
        double x = (origin.getX() + 0.5D) - (pos.getX() + 0.5D);
        double y = (origin.getY() + 0.5D) - (pos.getY() + 0.5D);
        double z = (origin.getZ() + 0.5D) - (pos.getZ() + 0.5D);
        return Math.sqrt(x * x + y * y + z * z) < distance;
    }

    public boolean isSolid(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
    }

    public void addNext(Set<BlockPos> list, BlockPos origin) {
        list.add(origin.offset(EnumFacing.NORTH));
        list.add(origin.offset(EnumFacing.SOUTH));
        list.add(origin.offset(EnumFacing.EAST));
        list.add(origin.offset(EnumFacing.WEST));
    }

    public List<BlockPos> getTerrainBlocks() {
        List<BlockPos> list = Lists.newArrayList();
        BlockPos[][] positions = this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
        for (int i = 0; i < cursor && i < positions.length; i++) {
            BlockPos[] position = positions[i];
            list.addAll(Arrays.asList(position));
        }
        return list;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        ListBlockPosExtendedData data = new ListBlockPosExtendedData();
        data.deserializeNBT(compound.getCompoundTag("SYNC_BLOCKS_DATA"));
        this.dataManager.set(SYNC_BLOCKS_DATA, data);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("SYNC_BLOCKS_DATA", this.dataManager.get(SYNC_BLOCKS_DATA).serializeNBT());
    }
}
