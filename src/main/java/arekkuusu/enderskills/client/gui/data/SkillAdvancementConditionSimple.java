package arekkuusu.enderskills.client.gui.data;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.widgets.GuiConfirmation;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Optional;
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

    public boolean upgrade() {
        if (info.skill instanceof ISkillAdvancement) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ISkillAdvancement.Requirement requirement = ((ISkillAdvancement) info.skill).getRequirement(player);
            Optional<AdvancementCapability> capability = Capabilities.advancement(player);
            if (capability.isPresent()) {
                AdvancementCapability c = capability.get();
                if (c.level >= requirement.getLevels() && c.getExperienceTotal(player) >= requirement.getXp()) {
                    PacketHelper.sendSkillUpgradeRequestPacket(Minecraft.getMinecraft().player, info.skill);
                    return true;
                } else {
                    String title = this.isUpgraded() ? "Can't upgrade" : "Can't Unlock";
                    String description = "\u00A74Not enough funds.\u00A7r";
                    GuiScreenSkillAdvancements.confirmation = new GuiConfirmation(Minecraft.getMinecraft(), title, description, (g) -> {
                    }, false, true, false);
                    GuiScreenSkillAdvancements.confirmation.initGui();
                    return false;
                }
            }
        } else {
            PacketHelper.sendSkillUpgradeRequestPacket(Minecraft.getMinecraft().player, info.skill);
            return true;
        }
        return false;
    }

    public void addCondition(SkillAdvancementCondition advancement) {
        this.conditions.add(advancement);
    }
}
