package arekkuusu.enderskills.client.gui.data;

public class SkillAdvancementConditionNotOrOverride extends SkillAdvancementConditionNot {

    public final SkillAdvancementCondition override;

    public SkillAdvancementConditionNotOrOverride(SkillAdvancementCondition advancement, SkillAdvancementCondition override) {
        super(advancement);
        this.override = override;
    }

    @Override
    public boolean isUpgraded() {
        return this.override.isUpgraded() || super.isUpgraded();
    }
}
