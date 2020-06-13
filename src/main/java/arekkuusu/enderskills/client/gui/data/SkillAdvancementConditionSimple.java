package arekkuusu.enderskills.client.gui.data;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.widgets.GuiConfirmation;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;

public class SkillAdvancementConditionSimple extends SkillAdvancementCondition {

    public final Set<SkillAdvancementCondition> conditions = Sets.newLinkedHashSet();
    public final SkillAdvancementInfo info;
    public final int row, column;

    public SkillAdvancementConditionSimple(SkillAdvancementInfo info, int row, int column) {
        this.info = info;
        this.row = row;
        this.column = column;
    }

    public boolean canUpgrade() {
        return this.conditions.isEmpty() || this.conditions.stream().allMatch(SkillAdvancementCondition::isUpgraded);
    }

    @Override
    public boolean isUpgraded() {
        return Capabilities.get(Minecraft.getMinecraft().player).filter(c ->
                c.owns(this.info.skill)
        ).isPresent();
    }

    public void upgrade() {
        if (info.skill instanceof ISkillAdvancement) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ISkillAdvancement.Requirement requirement = ((ISkillAdvancement) info.skill).getRequirement(player);
            Capabilities.advancement(player).ifPresent(c -> {
                if (c.level >= requirement.getLevels() && c.getExperienceTotal(player) >= requirement.getXp()) {
                    PacketHelper.sendSkillUpgradeRequestPacket(Minecraft.getMinecraft().player, info.skill);
                } else {
                    String title = this.isUpgraded() ? "Confirm Upgrade?" : "Confirm Unlock?";
                    String description = "\u00A74Not enough funds.\u00A7r";
                    GuiScreenSkillAdvancements.confirmation = new GuiConfirmation(Minecraft.getMinecraft(), title, description, () -> {}, false, true);
                    GuiScreenSkillAdvancements.confirmation.initGui();
                }
            });
        } else {
            PacketHelper.sendSkillUpgradeRequestPacket(Minecraft.getMinecraft().player, info.skill);
        }
    }

    public void addCondition(SkillAdvancementCondition advancement) {
        this.conditions.add(advancement);
    }
}
