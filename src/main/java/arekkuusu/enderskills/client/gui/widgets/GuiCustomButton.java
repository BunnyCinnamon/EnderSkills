package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

public class GuiCustomButton extends GuiBaseButton {

    public static final ResourceLocation WIDGETS = new ResourceLocation(LibMod.MOD_ID, "textures/gui/advancement/widgets.png");
    public final int textureX;
    public final int textureY;
    public final int textureWidth;
    public final int textureHeight;
    public final int color;

    public GuiCustomButton(int buttonId, int x, int y, String buttonText, int textureX, int textureY, int textureWidth, int textureHeight) {
        super(buttonId, x, y, buttonText);
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.color = 14737632;
    }

    public GuiCustomButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int textureX, int textureY, int textureWidth, int textureHeight, int color) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.color = color;
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
            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
