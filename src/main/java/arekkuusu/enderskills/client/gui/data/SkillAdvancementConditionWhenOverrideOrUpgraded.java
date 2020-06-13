package arekkuusu.enderskills.client.gui.data;

import java.util.Arrays;

public class SkillAdvancementConditionWhenOverrideOrUpgraded extends SkillAdvancementCondition {

    private final SkillAdvancementCondition original;
    public final SkillAdvancementCondition override;
    public final SkillAdvancementCondition[] advancements;

    public SkillAdvancementConditionWhenOverrideOrUpgraded(SkillAdvancementCondition original, SkillAdvancementCondition override, SkillAdvancementCondition... advancements) {
        this.original = original;
        this.override = override;
        this.advancements = advancements;
    }

    @Override
    public boolean canUpgrade() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpgraded() {
        return original.isUpgraded() || !this.override.isUpgraded() || Arrays.stream(advancements).allMatch(SkillAdvancementCondition::isUpgraded);
    }
}
