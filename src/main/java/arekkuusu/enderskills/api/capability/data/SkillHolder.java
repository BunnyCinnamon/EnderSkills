package arekkuusu.enderskills.api.capability.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;

public final class SkillHolder implements INBTSerializable<NBTTagCompound> {

    public SkillData data;
    public boolean dead;
    public int tick;

    public SkillHolder(SkillData data) {
        this.data = data;
        this.dead = false;
    }

    public SkillHolder(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    @Deprecated
    public void tick(EntityLivingBase entity) {
        if (tick == 0) {
            data.skill.begin(entity, data); //First tick
        }
        if ((data.time == -1 || (tick > 0 && data.time > 0 && tick < data.time)) && !isDead()) {
            data.skill.update(entity, data, tick); //Update tick
        }
        if (tick >= data.time && data.time != -1) {
            setDead(); //End my suffering
        }
        if (isDead()) {
            data.skill.end(entity, data); //Last tick
        }
        tick++;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead() {
        this.dead = true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBT(compound);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        readNBT(compound);
    }

    public void writeNBT(NBTTagCompound compound) {
        compound.setTag("data", data.serializeNBT());
        compound.setBoolean("dead", dead);
        compound.setInteger("tick", tick);
    }

    public void readNBT(NBTTagCompound compound) {
        data = new SkillData((NBTTagCompound) compound.getTag("data"));
        dead = compound.getBoolean("dead");
        tick = compound.getInteger("tick");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillHolder that = (SkillHolder) o;
        return dead == that.dead &&
                tick == that.tick &&
                data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, dead, tick);
    }
}
