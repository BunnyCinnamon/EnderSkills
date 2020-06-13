package arekkuusu.enderskills.common.entity.data;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class ListBlockStateExtendedData extends ExtendedData<ListBlockStateExtendedData> implements INBTSerializable<NBTTagCompound> {

    public static final ExtendedDataSerializer<ListBlockStateExtendedData> SERIALIZER = new ExtendedDataSerializer<>();

    static {
        register(ListBlockStateExtendedData.class);
    }

    public IBlockState[] states = new IBlockState[0];

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        states = new IBlockState[size];
        for (int i = 0; i < states.length; i++) {
            states[i] = Block.getStateById(buf.readInt());
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(states.length);
        for (IBlockState state : states) {
            buf.writeInt(Block.getStateId(state));
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("size", states.length);
        NBTTagList list = new NBTTagList();
        for (IBlockState state : states) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("id", Block.getStateId(state));
            list.appendTag(nbt);
        }
        tag.setTag("list", list);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        states = new IBlockState[nbt.getInteger("size")];
        NBTTagList list = nbt.getTagList("list", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < states.length; i++) {
            states[i] = Block.getStateById(list.getCompoundTagAt(i).getInteger("id"));
        }
    }
}
