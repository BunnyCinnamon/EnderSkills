package arekkuusu.enderskills.api.capability.data;

import arekkuusu.enderskills.api.capability.data.nbt.WatcherManager;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Objects;

public final class SkillData implements INBTSerializable<NBTTagCompound> {

    public WatcherManager watcher;
    public NBTTagCompound nbt;
    public Skill[] overrides;
    public Skill skill;
    public int time;

    public SkillData(NBTTagCompound nbt, Skill skill, Skill[] overrides, WatcherManager.Watcher[] trackers, int time) {
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
        NBTTagList list0 = new NBTTagList();
        for (Skill override : this.overrides) {
            NBTTagCompound sub = new NBTTagCompound();
            NBTHelper.setRegistry(sub, "skill", override);
            list0.appendTag(sub);
        }
        compound.setTag("overrides", list0);
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
        NBTTagList list0 = compound.getTagList("overrides", Constants.NBT.TAG_COMPOUND);
        this.overrides = new Skill[list0.tagCount()];
        for (int i = 0; i < list0.tagCount(); i++) {
            NBTTagCompound sub = list0.getCompoundTagAt(i);
            this.overrides[i] = NBTHelper.getRegistry(sub, "skill", Skill.class);
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
        public Skill skill;
        public Skill[] overrides;
        public NBTTagCompound tag;
        public WatcherManager.Watcher[] tracker;

        public Builder(Skill skill) {
            this.skill = skill;
            this.overrides = new Skill[0];
            this.tracker = new WatcherManager.Watcher[0];
            this.tag = new NBTTagCompound();
        }

        public Builder put(NBTTagCompound tag, WatcherManager.Watcher... tracker) {
            this.tag = tag.copy();
            this.tracker = tracker;
            return this;
        }

        public Builder with(int time) {
            this.time = time;
            return this;
        }

        public Builder overrides(Skill... overrides) {
            this.overrides = overrides;
            return this;
        }

        public SkillData create() {
            return new SkillData(tag, skill, overrides, tracker, time);
        }
    }
}
