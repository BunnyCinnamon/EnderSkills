package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.data.ListBlockPosExtendedData;
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
import java.util.Set;

public class EntityPlaceableShockwave extends EntityPlaceableData {

    public static final DataParameter<ListBlockPosExtendedData> SYNC_BLOCKS_DATA = EntityDataManager.createKey(EntityPlaceableShockwave.class, ListBlockPosExtendedData.SERIALIZER);
    public double[] curves = new double[0];
    public double cursor = 0;

    public EntityPlaceableShockwave(World world) {
        super(world);
    }

    public EntityPlaceableShockwave(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        super(worldIn, owner, skillData, lifeTime);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SYNC_BLOCKS_DATA, new ListBlockPosExtendedData());
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            updateSpread();
        }
        super.onUpdate();
    }

    public void updateSpread() {
        BlockPos[][] positions = this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
        curves = new double[positions.length];
        double perTick = (double) (curves.length + 6) / (double) (MIN_TIME * 2);
        double maxLayer = getData().nbt.getDouble("push") * 0.5D;
        double perLayer = maxLayer / 3;
        int num = (int) Math.ceil(cursor);
        for (int i = num - 3; i < num + 3; i++) {
            if (i < curves.length + 3 && i >= -3) {
                int diff = i - num;
                if (diff > 0) diff = -diff;
                double layer = maxLayer + (perLayer * (double) diff);
                if (i >= 0 && i < curves.length && layer > curves[i]) {
                    curves[i] = layer;
                }
            }
        }
        cursor += perTick;
    }

    public void spreadOnTerrain() {
        ListBlockPosExtendedData data = new ListBlockPosExtendedData();
        BlockPos[][] positions = this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
        int radius = (int) Math.ceil(getRadius());
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
                if (j == 2) return null;
                mPos.move(EnumFacing.DOWN);
                if (isSolid(mPos)) {
                    return mPos.toImmutable();
                }
            }
        } else if (isSolid(mPos.up())) {
            for (int j = 0; ; j++) {
                if (j == 2) return null;
                mPos.move(EnumFacing.UP);
                if (!isSolid(mPos.up())) {
                    return mPos.toImmutable();
                }
            }
        }
        return mPos.toImmutable();
    }

    public boolean isWithingRadius(BlockPos origin, BlockPos pos, int distance) {
        int x = origin.getX() - pos.getX();
        x = Math.abs(x);
        int y = origin.getY() - pos.getY();
        y = Math.abs(y);
        int z = origin.getZ() - pos.getZ();
        z = Math.abs(z);
        return x <= distance && y <= distance && z <= distance;
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

    public BlockPos[][] getTerrainBlocks() {
        return this.dataManager.get(SYNC_BLOCKS_DATA).posArray;
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
