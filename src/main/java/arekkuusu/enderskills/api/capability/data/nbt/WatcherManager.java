package arekkuusu.enderskills.api.capability.data.nbt;

import arekkuusu.enderskills.api.capability.data.SkillData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class WatcherManager {

    public static final List<Watcher> WATCHERS = Lists.newLinkedList();
    public final Map<Watcher, WatcherEntry> entries = Maps.newHashMap();

    public WatcherEntry get(Watcher watcher) {
        return entries.get(watcher);
    }

    public void add(Watcher watcher) {
        entries.put(watcher, watcher.entry());
    }

    public Watcher[] copy() {
        return entries.keySet().toArray(new WatcherManager.Watcher[0]);
    }

    public static abstract class Watcher {
        public abstract WatcherEntry entry();
    }

    public static abstract class WatcherEntry {
        public abstract void onRead(SkillData data);

        public abstract void onWrite(SkillData data);
    }

    static {
        //Nothing here
    }
}
