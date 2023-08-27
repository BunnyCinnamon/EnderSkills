package arekkuusu.enderskills.api.configuration.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ConfigSynchronizer extends IForgeRegistryEntry.Impl<ConfigSynchronizer> {

    public void initSyncConfig() {
    }

    public void writeSyncConfig(NBTTagCompound compound) {
    }

    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
    }

    public void update() {
    }
}
