package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionSimple;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Arrays;

public class GuiSkillAdvancementMultiple extends GuiSkillAdvancement {

    public final GuiSkillAdvancementSimple[] advancements;

    public GuiSkillAdvancementMultiple(GuiSkillAdvancementPage gui, SkillAdvancementConditionSimple... advancements) {
        super(gui);
        this.advancements = Arrays.stream(advancements).map(a -> new GuiSkillAdvancementSimple(gui, a)).toArray(GuiSkillAdvancementSimple[]::new);
    }

    @Override
    public void initGui() {
        for (GuiSkillAdvancementSimple advancement : advancements) {
            advancement.mc = this.mc;
            advancement.initGui();
        }
        this.x = advancements[0].x;
        this.y = advancements[0].y;
    }

    @Override
    public void drawGui(int mouseX, int mouseY, float partialTicks) {
        super.drawGui(mouseX, mouseY, partialTicks);
        for (GuiSkillAdvancementSimple advancement : advancements) {
            advancement.drawGui(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {
        this.mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        GlStateManager.color(1F, 1F, 1F, 0.95F);
        RenderMisc.draw8Rect(this.xOffset - 1, this.yOffset - 1, (this.advancements[this.advancements.length - 1].xOffset + 28) - this.advancements[0].xOffset, 28);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public int getConnectivityX() {
        return this.xOffset + ((this.advancements[this.advancements.length - 1].xOffset + 26) - this.advancements[0].xOffset) / 2;
    }

    @Override
    public boolean isUnlockable() {
        return Arrays.stream(this.advancements).anyMatch(GuiSkillAdvancementSimple::isUnlockable);
    }

    @Override
    public boolean isUnlocked() {
        return Arrays.stream(this.advancements).anyMatch(GuiSkillAdvancementSimple::isUnlocked);
    }

    @Override
    public void drawSkillToolTip(int mouseX, int mouseY, float partialTicks) {
        for (GuiSkillAdvancementSimple advancement : this.advancements) {
            if (advancement.isVisible() && advancement.isHovered()) {
                advancement.drawSkillToolTip(mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public boolean isHovered() {
        return Arrays.stream(this.advancements).anyMatch(GuiSkillAdvancementSimple::isHovered);
    }

    @Override
    public boolean isVisible() {
        return Arrays.stream(this.advancements).anyMatch(GuiSkillAdvancementSimple::isVisible);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiSkillAdvancementSimple advancement : this.advancements) {
            advancement.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        for (GuiSkillAdvancementSimple advancement : this.advancements) {
            advancement.actionPerformed(button);
        }
    }
}
