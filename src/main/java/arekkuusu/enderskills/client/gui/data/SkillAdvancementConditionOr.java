package arekkuusu.enderskills.client.gui.data;

import java.util.Arrays;

public class SkillAdvancementConditionOr extends SkillAdvancementCondition {

    public final SkillAdvancementCondition[] advancements;

    public SkillAdvancementConditionOr(SkillAdvancementCondition... advancements) {
        this.advancements = advancements;
    }

    public boolean canUpgrade() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpgraded() {
        return Arrays.stream(advancements).anyMatch(SkillAdvancementCondition::isUpgraded);
    }
}
