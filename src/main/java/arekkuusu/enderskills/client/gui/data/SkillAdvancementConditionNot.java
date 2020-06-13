package arekkuusu.enderskills.client.gui.data;

public class SkillAdvancementConditionNot extends SkillAdvancementCondition {

    public final SkillAdvancementCondition advancement;

    public SkillAdvancementConditionNot(SkillAdvancementCondition advancement) {
        this.advancement = advancement;
    }

    public boolean canUpgrade() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpgraded() {
        return !this.advancement.isUpgraded();
    }
}
