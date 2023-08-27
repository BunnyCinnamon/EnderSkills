package arekkuusu.enderskills.api.configuration.network;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.skill.ModConfigurations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class StaticFieldConfigSynchronizer extends ConfigSynchronizer {

    public final Field localField;
    public final Field remoteField;

    public StaticFieldConfigSynchronizer(Class<?> clss, String local, String remote, String name) {
        ModConfigurations.setRegistry(this, name);
        this.localField = ObfuscationReflectionHelper.findField(clss, local);
        this.remoteField = ObfuscationReflectionHelper.findField(clss, remote);
    }

    @Override
    public void initSyncConfig() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            this.localField.set(null, gson.fromJson(gson.toJson(this.remoteField.get(null)), localField.getType()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.update();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            NBTHelper.setString(compound, "config", gson.toJson(this.remoteField.get(null)));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            this.localField.set(null, gson.fromJson(NBTHelper.getString(compound, "config"), localField.getType()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.update();
    }

    @Override
    public void update() {

    }
}
