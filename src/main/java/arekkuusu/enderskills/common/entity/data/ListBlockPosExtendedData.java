package arekkuusu.enderskills.common.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class ListBlockPosExtendedData extends ExtendedData<ListBlockPosExtendedData> implements INBTSerializable<NBTTagCompound> {

    public static final ExtendedDataSerializer<ListBlockPosExtendedData> SERIALIZER = new ExtendedDataSerializer<>();

    static {
        register(ListBlockPosExtendedData.class);
    }

    public BlockPos[][] posArray = new BlockPos[0][0];

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        posArray = new BlockPos[size][0];
        for (int i = 0; i < posArray.length; i++) {
            int subSize = buf.readInt();
            BlockPos[] subArray = new BlockPos[subSize];
            for (int j = 0; j < subArray.length; j++) {
                subArray[j] = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            }
            posArray[i] = subArray;
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(posArray.length);
        for (BlockPos[] subArray : posArray) {
            buf.writeInt(subArray.length);
            for (BlockPos pos : subArray) {
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("h", posArray.length);
        NBTTagList hList = new NBTTagList();
        for (BlockPos[] subPosArray : posArray) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("v", subPosArray.length);
            NBTTagList vList = new NBTTagList();
            for (BlockPos blockPos : subPosArray) {
                NBTTagCompound subNbt = new NBTTagCompound();
                subNbt.setInteger("x", blockPos.getX());
                subNbt.setInteger("y", blockPos.getY());
                subNbt.setInteger("z", blockPos.getZ());
                vList.appendTag(subNbt);
            }
            nbt.setTag("vl", vList);
            hList.appendTag(nbt);
        }
        tag.setTag("hl", hList);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        posArray = new BlockPos[nbt.getInteger("h")][];
        NBTTagList hList = nbt.getTagList("hl", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < posArray.length; i++) {
            NBTTagCompound tag = hList.getCompoundTagAt(i);
            posArray[i] = new BlockPos[tag.getInteger("v")];
            NBTTagList vList = tag.getTagList("vl", Constants.NBT.TAG_COMPOUND);
            BlockPos[] pos = posArray[i];
            for (int i1 = 0; i1 < pos.length; i1++) {
                NBTTagCompound subTag = vList.getCompoundTagAt(i1);
                BlockPos blockPos = new BlockPos(
                        subTag.getInteger("x"),
                        subTag.getInteger("y"),
                        subTag.getInteger("z")
                );
                posArray[i][i1] = blockPos;
            }
        }
    }
}
