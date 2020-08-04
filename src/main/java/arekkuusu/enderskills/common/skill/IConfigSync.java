package arekkuusu.enderskills.common.skill;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IConfigSync {

    @Deprecated
    void initSyncConfig();

    @Deprecated
    void writeSyncConfig(NBTTagCompound compound);

    @SideOnly(Side.CLIENT)
    @Deprecated
    void readSyncConfig(NBTTagCompound compound);
}
