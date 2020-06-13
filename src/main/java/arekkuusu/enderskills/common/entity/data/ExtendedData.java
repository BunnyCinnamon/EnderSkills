package arekkuusu.enderskills.common.entity.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public abstract class ExtendedData<T> {

    private static Int2ObjectMap<Class<? extends ExtendedData<?>>> int2ObjectMap;
    private static Object2IntMap<Class<? extends ExtendedData<?>>> object2IntMap;
    private static int nextId = 0;

    public abstract void fromBytes(PacketBuffer buf);

    public abstract void toBytes(PacketBuffer buf);

    protected static int register(Class<? extends ExtendedData<?>> behaviorClass) {
        if (int2ObjectMap == null) {
            int2ObjectMap = new Int2ObjectArrayMap<>();
            object2IntMap = new Object2IntArrayMap<>();
        }
        int id = nextId++;
        int2ObjectMap.put(id, behaviorClass);
        object2IntMap.put(behaviorClass, id);
        return id;
    }

    public static ExtendedData<?> lookup(int id) {
        try {
            return int2ObjectMap.get(id).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return object2IntMap.get(getClass());
    }

    public static class ExtendedDataSerializer<T extends ExtendedData<?>> implements DataSerializer<T> {

        @Override
        public void write(PacketBuffer buf, T value) {
            buf.writeInt(value.getId());
            value.toBytes(buf);
        }

        @Override
        public T read(PacketBuffer buf) {
            try {
                ExtendedData<?> behavior = lookup(buf.readInt());
                behavior.fromBytes(buf);
                return (T) behavior;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public DataParameter<T> createKey(int id) {
            return new DataParameter<T>(id, this);
        }

        @Override
        public T copyValue(T behavior) {
            return behavior;
        }
    }
}
