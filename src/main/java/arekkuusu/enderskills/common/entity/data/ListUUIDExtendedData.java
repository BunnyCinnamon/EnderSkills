package arekkuusu.enderskills.common.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class ListUUIDExtendedData extends ExtendedData<ListUUIDExtendedData> implements INBTSerializable<NBTTagCompound> {

    public static final ExtendedDataSerializer<ListUUIDExtendedData> SERIALIZER = new ExtendedDataSerializer<>();

    static {
        register(ListUUIDExtendedData.class);
    }

    public UUID[] uuids = new UUID[0];

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        uuids = new UUID[size];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = buf.readUniqueId();
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(uuids.length);
        for (UUID uuid : uuids) {
            buf.writeUniqueId(uuid);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("size", uuids.length);
        NBTTagList list = new NBTTagList();
        for (UUID uuid : uuids) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setUniqueId("uuid", uuid);
            list.appendTag(nbt);
        }
        tag.setTag("list", list);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        uuids = new UUID[nbt.getInteger("size")];
        NBTTagList list = nbt.getTagList("list", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = list.getCompoundTagAt(i).getUniqueId("uuid");
        }
    }
}
