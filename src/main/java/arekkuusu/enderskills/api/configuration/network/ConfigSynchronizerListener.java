package arekkuusu.enderskills.api.configuration.network;

import arekkuusu.enderskills.common.skill.ModConfigurations;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ConfigSynchronizerListener extends ConfigSynchronizer {

    public ConfigSynchronizerListener(String name) {
        ModConfigurations.setRegistry(this, name);
    }

    @Override
    public void initSyncConfig() {
        this.update();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        this.initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        this.update();
    }

    @Override
    public abstract void update();
}
