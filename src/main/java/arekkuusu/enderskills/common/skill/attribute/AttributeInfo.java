package arekkuusu.enderskills.common.skill.attribute;

import arekkuusu.enderskills.api.capability.data.IInfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import net.minecraft.nbt.NBTTagCompound;

public class AttributeInfo extends SkillInfo implements IInfoUpgradeable {

    private int level;

    public AttributeInfo(NBTTagCompound tag) {
        super(tag);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger("level", level);
    }

    @Override
    public void readNBT(NBTTagCompound compound) {
        level = compound.getInteger("level");
    }
}
