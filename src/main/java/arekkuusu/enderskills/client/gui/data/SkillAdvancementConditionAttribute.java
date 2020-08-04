package arekkuusu.enderskills.client.gui.data;

import arekkuusu.enderskills.api.capability.Capabilities;
import net.minecraft.client.Minecraft;

public class SkillAdvancementConditionAttribute extends SkillAdvancementConditionSimple {

    public SkillAdvancementConditionAttribute(SkillAdvancementInfo info, int row, int column) {
        super(info, row, column);
    }

    public boolean canUpgrade() {
        return this.conditions.isEmpty() || this.conditions.stream().allMatch(SkillAdvancementCondition::isUpgraded);
    }

    @Override
    public boolean isUpgraded() {
        return Capabilities.get(Minecraft.getMinecraft().player).filter(c ->
                c.isOwned(this.info.skill)
        ).isPresent();
    }
}
