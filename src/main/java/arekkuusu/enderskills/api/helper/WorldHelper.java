package arekkuusu.enderskills.api.helper;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.WeakHashMap;

public class WorldHelper {

    public static final WeakHashMap<String, Entity> entityWeakHashMap = new WeakHashMap<>(); //Suck my programmer dick

    @Nullable
    public static <T extends Entity> T getEntity(Class<T> ent, World world, UUID key) {
        String sidedKey = key + ":" + world.isRemote;
        if (!entityWeakHashMap.containsKey(sidedKey)) {
            for (Entity entity : world.loadedEntityList) {
                if (ent.isAssignableFrom(ent) && entity.getUniqueID().equals(key)) {
                    entityWeakHashMap.put(sidedKey, entity);
                    return ent.cast(entity);
                }
            }
        } else {
            return ent.cast(entityWeakHashMap.get(sidedKey));
        }
        return null;
    }
}
