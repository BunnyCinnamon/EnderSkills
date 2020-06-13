package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionSimple;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiSkillAdvancementOr extends GuiSkillAdvancement {

    public final GuiSkillAdvancementSimple left;
    public final GuiSkillAdvancementSimple right;
    public int xText;
    public int yText;

    public GuiSkillAdvancementOr(GuiSkillAdvancementPage gui, SkillAdvancementConditionSimple left, SkillAdvancementConditionSimple right) {
        super(gui);
        this.left = new GuiSkillAdvancementSimple(gui, left);
        this.right = new GuiSkillAdvancementSimple(gui, right);
    }

    @Override
    public void initGui() {
        this.left.mc = this.mc;
        this.left.initGui();
        this.right.mc = this.mc;
        this.right.initGui();
        this.x = left.x;
        this.y = left.y;
        this.xText = 8 + (right.x - left.x) / 2;
        this.yText = 9 + (right.y - left.y) / 2;
    }

    @Override
    public void drawGui(int mouseX, int mouseY, float partialTicks) {
        super.drawGui(mouseX, mouseY, partialTicks);
        this.left.drawGui(mouseX, mouseY, partialTicks);
        this.right.drawGui(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {
        this.mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        GlStateManager.color(1F, 1F, 1F, 0.95F);
        RenderMisc.draw8Rect(this.xOffset - 1, this.yOffset - 1, (this.right.x + 28) - this.left.x, 28);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {
        drawHorizontalLine(this.left.xOffset + 24, this.left.xOffset + 29, this.left.yOffset + 13 - 1, 0x000000FF);
        drawHorizontalLine(this.left.xOffset + 24, this.left.xOffset + 29, this.left.yOffset + 13, 0xFFFFFFFF);
        drawHorizontalLine(this.left.xOffset + 24, this.left.xOffset + 29, this.left.yOffset + 13 + 1, 0x000000FF);
        drawHorizontalLine(this.right.xOffset + 3, this.right.xOffset - 3, this.right.yOffset + 13 - 1, 0x000000FF);
        drawHorizontalLine(this.right.xOffset + 3, this.right.xOffset - 3, this.right.yOffset + 13, 0xFFFFFFFF);
        drawHorizontalLine(this.right.xOffset + 3, this.right.xOffset - 3, this.right.yOffset + 13 + 1, 0x000000FF);
        drawString(mc.fontRenderer, "OR", this.xOffset + this.xText, this.yOffset + this.yText, -1);
    }

    @Override
    public int getConnectivityX() {
        return this.left.xOffset + ((right.xOffset + 26) - left.xOffset) / 2;
    }

    @Override
    public boolean isUnlockable() {
        return this.left.isUnlockable() && this.right.isUnlockable();
    }

    @Override
    public boolean isUnlocked() {
        return this.left.isUnlocked() || this.right.isUnlocked();
    }

    @Override
    public void drawSkillToolTip(int mouseX, int mouseY, float partialTicks) {
        if (this.left.isVisible() && this.left.isHovered())
            this.left.drawSkillToolTip(mouseX, mouseY, partialTicks);
        if (this.right.isVisible() && this.right.isHovered())
            this.right.drawSkillToolTip(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isHovered() {
        return left.isHovered() || right.isHovered();
    }

    @Override
    public boolean isVisible() {
        return left.isVisible() || right.isVisible();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.left.mouseClicked(mouseX, mouseY, mouseButton);
        this.right.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        this.left.actionPerformed(button);
        this.right.actionPerformed(button);
    }
}
