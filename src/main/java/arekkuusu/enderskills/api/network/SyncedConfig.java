package arekkuusu.enderskills.api.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class SyncedConfig implements INBTSerializable<NBTTagCompound> {

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBT(new NBTTagCompound());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        readNBT(compound);
    }

    public void writeNBT(NBTTagCompound compound) {
        //For Rent
    }

    public void readNBT(NBTTagCompound compound) {
        //For Rent
    }
}
