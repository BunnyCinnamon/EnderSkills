package arekkuusu.enderskills.client.gui.data;

public class SkillAdvancementConditionAltarUltimate extends SkillAdvancementConditionAltar {

    public SkillAdvancementConditionAltarUltimate() {
        super(0);
    }

    @Override
    public boolean isUpgraded() {
        return IS_ULTIMATE;
    }
}
