package arekkuusu.enderskills.common.entity;

import net.minecraft.entity.ai.EntityAIBase;

public class AIOverride extends EntityAIBase {

    public static final AIOverride INSTANCE = new AIOverride();

    @Override
    public int getMutexBits() {
        return 0xFFFFFF;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }
}
