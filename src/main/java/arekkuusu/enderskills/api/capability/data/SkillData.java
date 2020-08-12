package arekkuusu.enderskills.api.capability.data;

import arekkuusu.enderskills.api.capability.data.nbt.WatcherManager;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class SkillData implements INBTSerializable<NBTTagCompound> {

    public String id;
    public WatcherManager watcher;
    public Overrides overrides;
    public NBTTagCompound nbt;
    public Skill skill;
    public int time;

    public SkillData(NBTTagCompound nbt, String id, Skill skill, Overrides overrides, WatcherManager.Watcher[] trackers, int time) {
        this.id = id;
        this.nbt = nbt;
        this.time = time;
        this.skill = skill;
        this.overrides = overrides;
        this.watcher = new WatcherManager();
        for (WatcherManager.Watcher tracker : trackers) {
            this.watcher.add(tracker);
        }
    }

    public SkillData(NBTTagCompound tag) {
        this.deserializeNBT(tag);
    }

    public SkillData copy() {
        return new SkillData(serializeNBT());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeNBT(compound);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.readNBT(compound);
    }

    public void writeNBT(NBTTagCompound compound) {
        NBTHelper.setString(compound, "id", this.id);
        if (this.overrides != null) {
            NBTHelper.setInteger(compound, "overrides", this.overrides.ordinal());
        }
        NBTHelper.setRegistry(compound, "skill", this.skill);
        NBTHelper.setInteger(compound, "time", this.time);
        NBTHelper.setNBT(compound, "nbt", this.nbt);
        //Watcher
        NBTTagList list1 = new NBTTagList();
        for (WatcherManager.Watcher watcher : this.watcher.entries.keySet()) {
            list1.appendTag(new NBTTagInt(WatcherManager.WATCHERS.indexOf(watcher)));
        }
        this.watcher.entries.forEach((key, value) -> value.onWrite(this));
        compound.setTag("watchers", list1);
    }

    public void readNBT(NBTTagCompound compound) {
        this.id = NBTHelper.getString(compound, "id");
        if (NBTHelper.hasTag(compound, "overrides")) {
            this.overrides = Overrides.values()[NBTHelper.getInteger(compound, "overrides")];
        } else {
            this.overrides = null;
        }
        this.skill = NBTHelper.getRegistry(compound, "skill", Skill.class);
        this.time = compound.getInteger("time");
        this.nbt = compound.getCompoundTag("nbt");
        //Watcher
        NBTTagList list1 = compound.getTagList("watchers", Constants.NBT.TAG_INT);
        this.watcher = new WatcherManager();
        for (int i = 0; i < list1.tagCount(); i++) {
            this.watcher.add(WatcherManager.WATCHERS.get(list1.getIntAt(i)));
        }
        this.watcher.entries.forEach((key, value) -> value.onRead(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillData data = (SkillData) o;
        return nbt.equals(data.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nbt);
    }

    public static Builder of(Skill skill) {
        return new Builder(skill);
    }

    public static class Builder {

        public int time;
        public String id;
        public Skill skill;
        public NBTTagCompound tag;
        public Overrides overrides;
        public WatcherManager.Watcher[] tracker;

        public Builder(Skill skill) {
            this.skill = skill;
            this.id = UUID.randomUUID().toString();
            this.tag = new NBTTagCompound();
            this.overrides = Overrides.NONE;
            this.tracker = new WatcherManager.Watcher[0];
        }

        public Builder put(NBTTagCompound tag, WatcherManager.Watcher... tracker) {
            this.tag = tag.copy();
            this.tracker = tracker;
            return this;
        }

        public Builder overrides(Overrides overrides) {
            this.overrides = overrides;
            return this;
        }

        public Builder with(int time) {
            this.time = time;
            return this;
        }

        public Builder by(Entity owner) {
            return this.by(owner.getUniqueID().toString());
        }

        public Builder by(UUID id) {
            return this.by(id.toString());
        }

        public Builder by(UUID a, UUID b) {
            return this.by(a.toString() + ":" + b.toString());
        }

        public Builder by(String id) {
            this.id = id;
            return this;
        }

        public SkillData create() {
            return new SkillData(tag, id, skill, overrides, tracker, time);
        }
    }

    public enum Overrides {
        EQUAL,
        SAME,
        ID,
        NONE
    }
}
