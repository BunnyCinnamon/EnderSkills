package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiConfirmation extends Gui {

    public GuiButton buttonYes;
    public GuiButton buttonNo;
    public final Consumer<GuiConfirmation> function;
    public final Minecraft mc;
    public final String title;
    public final boolean canConfirm;
    public final boolean canNegate;
    public final boolean isShifting;
    public final String description;
    public boolean allowUserInput;
    public int timer;
    public int width;
    public int height;
    public int x;
    public int y;

    public GuiConfirmation(Minecraft mc, String title, String description, Consumer<GuiConfirmation> function, boolean canConfirm, boolean canNegate, boolean isShifting) {
        this.mc = mc;
        this.function = function;
        this.description = description;
        this.title = title;
        this.canConfirm = canConfirm;
        this.canNegate = canNegate;
        this.isShifting = isShifting;
        this.allowUserInput = true;
        this.timer = 5;
    }

    public void initGui() {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        this.width = scaledresolution.getScaledWidth();
        this.height = scaledresolution.getScaledHeight();
        this.x = width / 2;
        this.y = height / 2;
        int titleWidth = mc.fontRenderer.getStringWidth(title);
        List<String> descriptionLines = getDescriptionLines();
        List<String> descriptionFormattedLines = new ArrayList<>();
        for (String descriptionLine : descriptionLines) {
            int s;
            if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine))) {
                titleWidth = s;
            }
        }
        for (String descriptionLine : descriptionLines) {
            if (!descriptionLine.isEmpty()) {
                descriptionFormattedLines.addAll(TextHelper.findOptimalLines(this.mc, descriptionLine, titleWidth));
            } else {
                descriptionFormattedLines.add("");
            }
        }
        this.buttonYes = new GuiCustomButton(0, this.x - 25, this.y + 5 + descriptionFormattedLines.size() * this.mc.fontRenderer.FONT_HEIGHT, 12, 10, "", 0, 72, 12, 10, 0);
        this.buttonNo = new GuiCustomButton(1, this.x + 13, this.y + 5 + descriptionFormattedLines.size() * this.mc.fontRenderer.FONT_HEIGHT, 11, 11, "", 12, 72, 11, 11, 0);
        buttonYes.enabled = false;
        buttonNo.enabled = false;
    }

    public void update() {
        if(this.timer > 0) this.timer--;
        if(timer == 0) {
            buttonYes.enabled = allowUserInput;
            buttonNo.enabled = allowUserInput;
        }
    }

    public void drawGui(int mouseX, int mouseY, float partialTicks) {
        this.drawGradientRect(0, 0, this.mc.displayWidth, this.mc.displayHeight, -1072689136, -804253680);
        this.mc.renderEngine.bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        int titleWidth = mc.fontRenderer.getStringWidth(title);

        List<String> descriptionLines = Lists.newArrayList(description.split("\n"));
        List<String> descriptionFormattedLines = new ArrayList<>();
        for (String descriptionLine : descriptionLines) {
            int s;
            if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine))) {
                titleWidth = s;
            }
        }
        boolean recalculateWidth = false;
        if (titleWidth > this.width / 3) {
            titleWidth = this.width / 3;
            recalculateWidth = true;
        }
        for (String descriptionLine : descriptionLines) {
            if (!descriptionLine.isEmpty()) {
                descriptionFormattedLines.addAll(TextHelper.findOptimalLines(this.mc, descriptionLine, titleWidth));
            } else {
                descriptionFormattedLines.add("");
            }
        }
        descriptionLines = descriptionFormattedLines;
        if (recalculateWidth) {
            titleWidth = mc.fontRenderer.getStringWidth(title);

            for (String descriptionLine : descriptionLines) {
                int s;
                if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine))) {
                    titleWidth = s;
                }
            }
        }

        int textureWidth = Math.max(titleWidth + 8, 50);
        int xOffset = this.x - (textureWidth / 2);
        int yOffset = this.y - 26;
        //Render Button Background
        //List<String> descriptionLines = findOptimalLines(description, titleWidth);
        RenderMisc.draw8Rect(xOffset, yOffset, textureWidth, 50 + descriptionLines.size() * this.mc.fontRenderer.FONT_HEIGHT);
        //Render Title Background
        this.drawTexturedModalRect(xOffset - 4, yOffset + 3, 0, 103, 4, 26);
        drawScaledCustomSizeModalRect(xOffset, yOffset + 3, 5, 103, 1, 26, textureWidth, 26, 256, 256);
        this.drawTexturedModalRect(xOffset + textureWidth, yOffset + 3, 7, 103, 4, 26);
        if (isShifting) {
            drawScaledCustomSizeModalRect(xOffset - 7, yOffset - 5, 108, 4, 18, 18, 14, 14, 256, 256);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.6D, 0.6D, 0.6D);
            this.mc.fontRenderer.drawString(TextHelper.translate("gui.advancement.locked"), (float) (xOffset + 9) / 0.6F, (float) (yOffset - 1) / 0.6F, -1, true);
            double mSize = Math.pow(0.6D, -1D);
            GlStateManager.scale(mSize, mSize, mSize);
            GlStateManager.popMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.fontRenderer.drawString(title, (float) (xOffset + 4), (float) (yOffset + 9), -1, true);
        GlStateManager.color(1F, 1F, 1F, 1F);
        for (int i = 0; i < descriptionLines.size(); ++i) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.mc.fontRenderer.drawString(descriptionLines.get(i), (float) (xOffset + 4), (float) (yOffset + 9 + 17 + i * this.mc.fontRenderer.FONT_HEIGHT), -1, false);
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        if (!canConfirm) {
            GlStateManager.color(0.1F, 0.1F, 0.1F, 1F);
        }
        this.buttonYes.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (!canConfirm) {
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        if (!canNegate) {
            GlStateManager.color(0.1F, 0.1F, 0.1F, 1F);
        }
        this.buttonNo.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (!canNegate) {
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }

    public List<String> getDescriptionLines() {
        int titleWidth = mc.fontRenderer.getStringWidth(title);

        List<String> descriptionLines = Lists.newArrayList(description.split("\n"));
        List<String> descriptionFormattedLines = new ArrayList<>();
        for (String descriptionLine : descriptionLines) {
            int s;
            if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine) - 25)) {
                titleWidth = s;
            }
        }
        if (titleWidth > this.width / 3) {
            titleWidth = this.width / 3;
        }
        for (String descriptionLine : descriptionLines) {
            if (!descriptionLine.isEmpty()) {
                descriptionFormattedLines.addAll(TextHelper.findOptimalLines(this.mc, descriptionLine, titleWidth + 20));
            } else {
                descriptionFormattedLines.add("");
            }
        }
        descriptionLines = descriptionFormattedLines;
        return descriptionLines;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (canConfirm && buttonYes.mousePressed(this.mc, mouseX, mouseY)) {
                buttonYes.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(buttonYes);
            }
            if (canNegate && buttonNo.mousePressed(this.mc, mouseX, mouseY)) {
                buttonNo.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(buttonNo);
            }
        }
    }

    public void actionPerformed(GuiButton button) {
        if (button.id == 0 || button.id == 1) {
            GuiScreenSkillAdvancements.confirmation = null;
        }
        if (button.id == 0) {
            confirm();
        }
        this.allowUserInput = false;
    }

    public void confirm() {
        this.mc.addScheduledTask(() -> this.function.accept(this));
    }
}
