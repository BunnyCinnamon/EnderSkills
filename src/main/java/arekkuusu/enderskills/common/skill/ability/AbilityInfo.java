package arekkuusu.enderskills.common.skill.ability;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import net.minecraft.nbt.NBTTagCompound;

public class AbilityInfo extends SkillInfo implements InfoUpgradeable, InfoCooldown {

    private int level;
    private int cooldown;

    public AbilityInfo(NBTTagCompound tag) {
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
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public boolean hasCooldown() {
        return getCooldown() > 0;
    }

    @Override
    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger(LEVEL, level);
        compound.setInteger(COOL_DOWN, cooldown);
    }

    @Override
    public void readNBT(NBTTagCompound compound) {
        level = compound.getInteger(LEVEL);
        cooldown = compound.getInteger(COOL_DOWN);
    }
}
