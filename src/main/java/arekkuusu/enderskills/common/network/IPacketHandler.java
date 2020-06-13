package arekkuusu.enderskills.common.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IPacketHandler {

    void handleData(NBTTagCompound compound, MessageContext context);
}
