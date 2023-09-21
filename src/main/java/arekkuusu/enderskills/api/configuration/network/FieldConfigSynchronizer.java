package arekkuusu.enderskills.api.configuration.network;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.skill.ModConfigurations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class FieldConfigSynchronizer<T> extends ConfigSynchronizer {

    public final Field field;
    public final Object local;
    public final Object remote;

    public FieldConfigSynchronizer(Class<?> clss, String field, T local, T remote, String name) {
        this.local = local;
        this.remote = remote;
        ModConfigurations.setRegistry(this, name);
        this.field = ObfuscationReflectionHelper.findField(clss, field);
    }

    @Override
    public void initSyncConfig() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            this.field.set(this.local, gson.fromJson(gson.toJson(this.field.get(this.remote)), this.field.getType()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.update();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            NBTHelper.setString(compound, "config", gson.toJson(this.field.get(this.remote)));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.initSyncConfig();
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            this.field.set(this.local, gson.fromJson(NBTHelper.getString(compound, "config"), this.field.getType()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.update();
    }

    @Override
    public void update() {

    }
}
