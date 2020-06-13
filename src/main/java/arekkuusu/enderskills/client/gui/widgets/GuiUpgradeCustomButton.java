package arekkuusu.enderskills.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiUpgradeCustomButton extends GuiCustomButton {

    public GuiUpgradeCustomButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int textureX, int textureY, int textureWidth, int textureHeight, int color) {
        super(buttonId, x, y, widthIn, heightIn, buttonText, textureX, textureY, textureWidth, textureHeight, color);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(WIDGETS);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int hoverState = this.getHoverState(this.hovered) - 1;
            drawModalRectWithCustomSizedTexture(this.x, this.y, this.textureX, this.textureY + hoverState * this.textureHeight, this.textureWidth, this.textureHeight, 256, 256);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = this.color;
            if (packedFGColour != 0) {
                color = packedFGColour;
            } else if (!this.enabled) {
                color = 10526880;
            } else if (this.hovered) {
                color = 16777120;
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width - (fontrenderer.getStringWidth(displayString) / 2), this.y, color);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
