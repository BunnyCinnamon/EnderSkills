package arekkuusu.enderskills.api.capability.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class SkillInfo implements INBTSerializable<NBTTagCompound> {

    public SkillInfo(NBTTagCompound tag) {
        deserializeNBT(tag);
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
        //For Rent
    }

    public void readNBT(NBTTagCompound compound) {
        //For Rent
    }

    public interface IInfoCooldown {

        String COOL_DOWN = "cooldown";

        void setCooldown(int cooldown);

        int getCooldown();

        boolean hasCooldown();
    }

    public interface IInfoUpgradeable {

        String LEVEL = "level";

        int getLevel();

        void setLevel(int level);
    }
}
