package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;

import java.util.function.IntSupplier;

public abstract class BaseSkill extends Skill implements IConfigSync, ISkillAdvancement {

    public static final int INDEFINITE = -1;
    public static final int INSTANT = 0;

    public BaseSkill(Properties properties) {
        super(properties);
    }

    public static class BaseProperties extends Properties {

        public IntSupplier maxLevelFunction;

        public BaseProperties setMaxLevelGetter(IntSupplier maxLevelFunction) {
            this.maxLevelFunction = maxLevelFunction;
            return this;
        }

        public int getMaxLevel() {
            return maxLevelFunction.getAsInt();
        }
    }
}
