package arekkuusu.enderskills.client.gui;

import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionAltar;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionSimple;
import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancement;
import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancementMultiple;
import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancementOr;
import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancementSimple;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiSkillAdvancementPage extends Gui {

    public final List<GuiSkillAdvancement> advancements = Lists.newLinkedList();
    public final GuiScreenSkillAdvancements gui;
    public final GuiSkillAdvancementTab tab;
    public final ITextComponent title;
    public Minecraft mc;
    public int x;
    public int y;
    public int scrollX;
    public int scrollY;
    public int minX;
    public int minY;
    public int maxX;
    public int maxY;
    public boolean centered;
    public boolean hover;
    public float fade;

    public GuiSkillAdvancementPage(GuiScreenSkillAdvancements gui, GuiSkillAdvancementTab tab, ITextComponent title) {
        this.gui = gui;
        this.tab = tab;
        this.title = title;
    }

    public void initGui() {
        this.x = gui.x;
        this.y = gui.y;
        this.minX = gui.minBoundaryX;
        this.minY = gui.minBoundaryY;
        this.maxX = gui.maxBoundaryX;
        this.maxY = gui.maxBoundaryY;
        for (GuiSkillAdvancement gui : advancements) {
            gui.mc = this.mc;
            gui.initGui();
            int i = gui.x;
            int j = i + 26;
            int k = gui.y;
            int l = k + 26;
            this.minX = Math.min(this.minX, i);
            this.maxX = Math.max(this.maxX, j);
            this.minY = Math.min(this.minY, k);
            this.maxY = Math.max(this.maxY, l);
        }
    }

    public void updateScreen() {
        if (!this.centered) {
            this.scrollX = ((gui.maxBoundaryX - gui.minBoundaryX) / 2) - ((this.maxX - this.minX) / 2);
            this.centered = true;
        }
        for (GuiSkillAdvancement advancement : advancements) {
            advancement.updateScreen();
        }
    }

    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {
    }

    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.depthFunc(515);
        RenderMisc.drawRect(gui.minBoundaryX, gui.minBoundaryY, gui.maxBoundaryX, gui.maxBoundaryY, 0x00000000);
        GlStateManager.depthFunc(518);
        //Draw Altar Power
        this.mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        int x = minX + scrollX + (21 + 26 * 3);
        int y = minY + scrollY + 5;
        int height = 18 * 7;
        this.drawTexturedModalRect(x, y, 140, 51, 5, 4);
        drawScaledCustomSizeModalRect(x, y + 4, 140, 56, 5, 1, 5, height, 256, 256);
        this.drawTexturedModalRect(x, y + height + 4, 140, 58, 5, 4);
        if (SkillAdvancementConditionAltar.ALTAR_JUICE > 0) {
            double h0 = 1D / (height + 8D); //% to fill for one cell
            double h = h0 * 4D; //% to fill for all cells up to target cell
            this.drawTexturedModalRect(x, y, 145, 51, 5, SkillAdvancementConditionAltar.ALTAR_JUICE > h ? 4 : (int) (4D * ((h - SkillAdvancementConditionAltar.ALTAR_JUICE) / h)));
            for (int i = 0; i <= Math.ceil(height * (SkillAdvancementConditionAltar.ALTAR_JUICE - h0 * 2D)); i++) {
                this.drawTexturedModalRect(x, y + 4 + i, 145, 56, 5, 1);
            }
            h0 = h;
            h = (height + 4D) / (height + 8D);
            this.drawTexturedModalRect(x, y + 4 + height, 145, 58, 5, SkillAdvancementConditionAltar.ALTAR_JUICE < h ? 0 : (int) (((SkillAdvancementConditionAltar.ALTAR_JUICE - h)) / h0));
        }
        //Draw Altar Bars
        int yOffset = minY + scrollY;
        this.drawTexturedModalRect(x - 1, yOffset + 5 + 22, 140, 64, 7, 3);
        this.drawTexturedModalRect(x - 1, yOffset + 5 + 60, 140, 64, 7, 3);
        this.drawTexturedModalRect(x - 1, yOffset + 5 + 96, 140, 64, 7, 3);
        this.drawTexturedModalRect(x - 1, yOffset + height + 8, 140, 64, 7, 3);
        //Draw Advancement
        if (advancements.size() > 0) {
            for (GuiSkillAdvancement advancement : advancements) {
                advancement.drawConnectivity(true);
                advancement.drawConnectivity(false);
            }
            for (GuiSkillAdvancement advancement : advancements) {
                advancement.drawGui(mouseX, mouseY, partialTicks);
            }
        }
        //Draw Fade Overlay
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawRect(gui.minBoundaryX, gui.minBoundaryY, gui.maxBoundaryX, gui.maxBoundaryY, MathHelper.floor(this.fade * 255.0F) << 24);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public void drawToolTips(int mouseX, int mouseY, float partialTicks) {
        //Draw Hover
        if (advancements.size() > 0) {
            for (GuiSkillAdvancement advancement : advancements) {
                if (advancement.isVisible() && advancement.isHovered()) {
                    advancement.drawSkillToolTip(mouseX, mouseY, partialTicks);
                    this.hover = true;
                }
            }
        }
        //Draw bar hover
        int x = minX + scrollX + (21 + 26 * 3);
        int y = minY + scrollY + 5;
        int height = 18 * 7 + 8;
        int width = 5;
        if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
            double progress = SkillAdvancementConditionAltar.ALTAR_JUICE;
            String text = "";
            if (progress < SkillAdvancementConditionAltar.LEVEL_0) {
                text = TextHelper.translate("gui.altar_power", TextHelper.format2FloatPoint(100 * (progress / SkillAdvancementConditionAltar.LEVEL_0)) + "%");
            } else if (progress < SkillAdvancementConditionAltar.LEVEL_1) {
                text = TextHelper.translate("gui.altar_power", TextHelper.format2FloatPoint(100 * ((progress - SkillAdvancementConditionAltar.LEVEL_0) / (SkillAdvancementConditionAltar.LEVEL_1 - SkillAdvancementConditionAltar.LEVEL_0))) + "%");
            } else if (progress < SkillAdvancementConditionAltar.LEVEL_2) {
                text = TextHelper.translate("gui.altar_power", TextHelper.format2FloatPoint(100 * ((progress - SkillAdvancementConditionAltar.LEVEL_1) / (SkillAdvancementConditionAltar.LEVEL_2 - SkillAdvancementConditionAltar.LEVEL_1))) + "%");
            } else if (progress < SkillAdvancementConditionAltar.LEVEL_3) {
                text = TextHelper.translate("gui.altar_power",  TextHelper.format2FloatPoint(100 * ((progress - SkillAdvancementConditionAltar.LEVEL_2) / (SkillAdvancementConditionAltar.LEVEL_3 - SkillAdvancementConditionAltar.LEVEL_2))) + "%");
            } else {
                text = TextHelper.translate("gui.altar_power_full");
            }
            this.gui.drawHoveringText(text, mouseX, mouseY);
        }
        if (this.hover) {
            this.fade = MathHelper.clamp(this.fade + 0.02F, 0.0F, 0.3F);
            this.hover = false;
        } else {
            this.fade = MathHelper.clamp(this.fade - 0.04F, 0.0F, 1.0F);
        }
    }

    public void scroll(int p_191797_1_, int p_191797_2_) {
        if (this.maxX - this.gui.minBoundaryX > 236) {
            this.scrollX = MathHelper.clamp(this.scrollX + p_191797_1_, -(this.maxX - 234), 0);
        }

        if (this.maxY - this.gui.minBoundaryY > 154) {
            this.scrollY = MathHelper.clamp(this.scrollY + p_191797_2_, -(this.maxY - 113), 0);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (GuiSkillAdvancement advancement : this.advancements) {
            advancement.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public GuiSkillAdvancementSimple addAdvancement(SkillAdvancementConditionSimple advancement) {
        GuiSkillAdvancementSimple gui = new GuiSkillAdvancementSimple(this, advancement);
        this.advancements.add(gui);
        return gui;
    }

    public GuiSkillAdvancementOr addAdvancement(SkillAdvancementConditionSimple advancement0, SkillAdvancementConditionSimple advancement1) {
        GuiSkillAdvancementOr gui = new GuiSkillAdvancementOr(this, advancement0, advancement1);
        this.advancements.add(gui);
        return gui;
    }

    public GuiSkillAdvancementMultiple addAdvancement(SkillAdvancementConditionSimple... advancements) {
        GuiSkillAdvancementMultiple gui = new GuiSkillAdvancementMultiple(this, advancements);
        this.advancements.add(gui);
        return gui;
    }
}
