package arekkuusu.enderskills.api.capability.data.nbt;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class UUIDWatcher extends WatcherManager.Watcher {

    public static final UUIDWatcher INSTANCE = new UUIDWatcher();

    public static class UUIDWatcherEntry extends WatcherManager.WatcherEntry {

        @Override
        public void onWrite(SkillData data) {
            NBTTagCompound nbt = NBTHelper.getNBTTag(data.nbt, "entity");
            for (String s : nbt.getKeySet()) {
                Entity entity = NBTHelper.getEntity(Entity.class, data.nbt, s);
                if (entity != null) {
                    NBTTagCompound tag = NBTHelper.getNBTTag(nbt, s);
                    tag.setInteger("id", entity.getEntityId());
                }
            }
        }

        @Override
        public void onRead(SkillData data) {
            NBTTagCompound nbt = NBTHelper.getNBTTag(data.nbt, "entity");
            for (String s : nbt.getKeySet()) {
                NBTTagCompound tag = NBTHelper.getNBTTag(nbt, s);
                Entity entity = NBTHelper.getEntity(Entity.class, data.nbt, s);
                World world = NBTHelper.getWorld(tag, "world");
                if (entity == null && world.isRemote) {
                    entity = world.getEntityByID(tag.getInteger("id"));
                    if (entity != null) {
                        NBTHelper.setEntity(data.nbt, entity, s);
                    }
                }
            }
        }
    }

    @Override
    public WatcherManager.WatcherEntry entry() {
        return new UUIDWatcherEntry();
    }
}
