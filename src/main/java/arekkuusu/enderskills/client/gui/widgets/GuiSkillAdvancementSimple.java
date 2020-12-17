package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionSimple;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementInfo;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.skill.BaseSkill;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.advancements.AdvancementState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiSkillAdvancementSimple extends GuiSkillAdvancement {

    public final SkillAdvancementConditionSimple advancement;
    public final int textureX = 0;
    public final int textureY = 0;
    public final int height = 26;
    public final int width = 26;
    public boolean hovered;
    public boolean visible;

    public GuiSkillAdvancementSimple(GuiSkillAdvancementPage gui, SkillAdvancementConditionSimple advancement) {
        super(gui);
        this.advancement = advancement;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.x = gui.minX + (11 + 26 * advancement.column);
        this.y = gui.minY + (5 + 18 * advancement.row);
        this.buttonList.add(new GuiUpgradeCustomButton(0, this.x, this.y, width, height, "+", 0, 0, 0, 0, 8453920));
    }

    public void drawGui(int mouseX, int mouseY, float partialTicks) {
        super.drawGui(mouseX, mouseY, partialTicks);
        this.hovered = mouseX >= this.xOffset && mouseY >= this.yOffset && mouseX < this.xOffset + this.width
                && mouseY < this.yOffset + this.height;
        this.visible = this.xOffset >= gui.gui.minBoundaryX && this.yOffset >= gui.gui.minBoundaryY
                & this.xOffset + this.width < gui.gui.maxBoundaryX && this.yOffset + this.height < gui.gui.maxBoundaryY;
    }

    @Override
    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {
        if (advancement.info.frame == SkillAdvancementInfo.Frame.NONE)
            return;

        mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        int xOffset = textureX + advancement.info.frame.getIcon();
        int yOffset = textureY + (isUnlockable() ? 0 : 26);

        if (isUnlockable()) {
            float r = (this.gui.tab.color >>> 16 & 0xFF) / 256F;
            float g = (this.gui.tab.color >>> 8 & 0xFF) / 256F;
            float b = (this.gui.tab.color & 0xFF) / 256F;
            GlStateManager.color(r, g, b, 1F);
        }
        drawScaledCustomSizeModalRect(this.xOffset, this.yOffset, xOffset, yOffset, 26, 26, width, height, 256, 256);
        if (isUnlockable()) {
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {
        this.drawSkillIcon();
        if (isUnlocked() && !isHovered() && canUpgrade()) {
            for (GuiButton guiButton : this.buttonList) {
                int oldX = guiButton.x;
                int oldY = guiButton.y;
                guiButton.x = guiButton.x + this.gui.scrollX;
                guiButton.y = guiButton.y + this.gui.scrollY;
                guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
                guiButton.x = oldX;
                guiButton.y = oldY;
            }
        }
        this.drawSkillLock();
    }

    public void drawSkillIcon() {
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(ResourceLibrary.getSkillTexture(advancement.info.skill).toString());
        drawTexturedModalRect(this.xOffset + 5, this.yOffset + 5, sprite, width - 10, height - 10);
        Capabilities.get(mc.player).flatMap(c -> c.getOwned(advancement.info.skill)).ifPresent(skillInfo -> {
            if (advancement.info.skill.getProperties() instanceof BaseSkill.BaseProperties && skillInfo instanceof SkillInfo.IInfoUpgradeable) {
                int skillTier = ((SkillInfo.IInfoUpgradeable) skillInfo).getLevel();
                if (skillTier > 0) {
                    String tier = String.valueOf(skillTier);
                    if (skillTier > 9999) tier = "9999+";
                    drawString(mc.fontRenderer, tier, this.xOffset + 18 + 10 - mc.fontRenderer.getStringWidth(tier),
                            this.yOffset + 18 + mc.fontRenderer.FONT_HEIGHT / 2, -1);
                }
            }
        });
    }

    public void drawSkillLock() {
        if (!isUnlocked()) {
            mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
            int lockType = !isUnlockable() ? 82 : 108;
            drawScaledCustomSizeModalRect(this.xOffset + 4, this.yOffset + 4, lockType, 4, 18, 18, 18, 18, 256, 256);
        }
    }

    @Override
    public boolean isUnlockable() {
        return this.advancement.canUpgrade();
    }

    @Override
    public boolean isUnlocked() {
        return Capabilities.get(this.mc.player).filter(c -> c.isOwned(this.advancement.info.skill)).isPresent();
    }

    @Override
    public void drawSkillToolTip(int mouseX, int mouseY, float partialTicks) {
        drawHover(mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void drawHover(int mouseX, int mouseY, float partialTicks) {
        String title = this.advancement.info.title.getFormattedText();
        int titleWidth = mc.fontRenderer.getStringWidth(title);
        String description = this.advancement.info.description.getFormattedText();
        List<String> descriptionLines = Lists.newArrayList(description.split("\n"));
        if (this.advancement.info.skill instanceof ISkillAdvancement) {
            ((ISkillAdvancement) this.advancement.info.skill).addDescription(descriptionLines);
        }
        List<String> descriptionFormattedLines = new ArrayList<>();
        for (String descriptionLine : descriptionLines) {
            int s;
            if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine) - 25)) {
                titleWidth = s;
            }
        }
        boolean recalculateWidth = false;
        if (titleWidth > this.gui.gui.width / 3) {
            titleWidth = this.gui.gui.width / 3;
            recalculateWidth = true;
        }
        for (String descriptionLine : descriptionLines) {
            if (!descriptionLine.isEmpty()) {
                descriptionFormattedLines.addAll(TextHelper.findOptimalLines(this.mc, descriptionLine, titleWidth + 20));
            } else {
                descriptionFormattedLines.add("");
            }
        }
        descriptionLines = descriptionFormattedLines;
        if (recalculateWidth) {
            titleWidth = mc.fontRenderer.getStringWidth(title);

            for (String descriptionLine : descriptionLines) {
                int s;
                if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine) - 25)) {
                    titleWidth = s;
                }
            }
        }
        int descriptionHeight = 32 + descriptionLines.size() * this.mc.fontRenderer.FONT_HEIGHT;
        boolean fitsTitle = this.xOffset + titleWidth + 26 < this.gui.gui.width;
        boolean fitsDescription = this.yOffset + descriptionHeight + 26 < this.gui.gui.height;
        int textureWidth = titleWidth + 26 + 8;
        int textureHeight = descriptionHeight;
        int fitXOffset = fitsTitle ? this.xOffset : this.xOffset + 26 - textureWidth;
        int fitYOffset = fitsDescription ? this.yOffset : this.yOffset + 26 - 4 - textureHeight;
        //Render textures
        AdvancementState advancement_state = isUnlocked() ? AdvancementState.OBTAINED : AdvancementState.UNOBTAINED;
        this.mc.getTextureManager().bindTexture(GuiScreenSkillAdvancements.WIDGETS);
        //Render Description Background
        RenderMisc.draw8Rect(fitXOffset, fitYOffset + 4, textureWidth, textureHeight - 4);
        //Render Title Background
        this.drawTexturedModalRect(fitXOffset - 4, this.yOffset + 3, 0, 103 + advancement_state.getId() * 26, 4, 26);
        drawScaledCustomSizeModalRect(fitXOffset, this.yOffset + 3, 5, 103 + advancement_state.getId() * 26, 1, 26, textureWidth, 26, 256, 256);
        this.drawTexturedModalRect(fitXOffset + textureWidth, this.yOffset + 3, 7, 103 + advancement_state.getId() * 26, 4, 26);
        //Render Icon Background
        int xOffset = textureX + advancement.info.frame.getIcon();
        int yOffset = textureY + (isUnlockable() ? 0 : 26);

        if (isUnlockable()) {
            float r = (this.gui.tab.color >>> 16 & 0xFF) / 256F;
            float g = (this.gui.tab.color >>> 8 & 0xFF) / 256F;
            float b = (this.gui.tab.color & 0xFF) / 256F;
            GlStateManager.color(r, g, b, 1F);
        }
        drawScaledCustomSizeModalRect(this.xOffset, this.yOffset, xOffset, yOffset, 26, 26, this.width, this.height, 256, 256);
        if (isUnlockable()) {
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        //Render Icon
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(ResourceLibrary.getSkillTexture(advancement.info.skill).toString());
        drawTexturedModalRect(this.xOffset + 5, this.yOffset + 5, sprite, 16, 16);
        //Render Title
        if (fitsTitle) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.mc.fontRenderer.drawString(title, (float) (fitXOffset + 30), (float) (this.yOffset + 9), -1, true);
            GlStateManager.color(1F, 1F, 1F, 1F);
        } else {
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.mc.fontRenderer.drawString(title, (float) (fitXOffset + 5), (float) (this.yOffset + 9), -1, true);
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        //Render Description
        if (fitsDescription) {
            for (int i = 0; i < descriptionLines.size(); ++i) {
                GlStateManager.color(1F, 1F, 1F, 1F);
                this.mc.fontRenderer.drawString(descriptionLines.get(i), (float) (fitXOffset + 5), (float) (this.yOffset + 9 + 17 + i * this.mc.fontRenderer.FONT_HEIGHT), -5592406, false);
                GlStateManager.color(1F, 1F, 1F, 1F);
            }
        } else {
            for (int i = 0; i < descriptionLines.size(); ++i) {
                GlStateManager.color(1F, 1F, 1F, 1F);
                this.mc.fontRenderer.drawString(descriptionLines.get(i), (float) (fitXOffset + 5), (float) (this.yOffset + 26 - descriptionHeight + 7 + i * this.mc.fontRenderer.FONT_HEIGHT), -5592406, false);
                GlStateManager.color(1F, 1F, 1F, 1F);
            }
        }

        if ((isUnlockable() || isUnlocked()) && canUpgrade()) {
            for (GuiButton guiButton : this.buttonList) {
                int oldX = guiButton.x;
                int oldY = guiButton.y;
                guiButton.x = guiButton.x + this.gui.scrollX;
                guiButton.y = guiButton.y + this.gui.scrollY;
                guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
                guiButton.x = oldX;
                guiButton.y = oldY;
            }
        }
    }

    public boolean canUpgrade() {
        boolean upgrade = true;
        SkilledEntityCapability c = Capabilities.get(this.mc.player).orElse(null);
        if (c != null && c.isOwned(advancement.info.skill)) {
            SkillInfo info = c.getOwned(advancement.info.skill).orElse(null);
            if (advancement.info.skill.getProperties() instanceof BaseSkill.BaseProperties && info instanceof SkillInfo.IInfoUpgradeable) {
                if (((SkillInfo.IInfoUpgradeable) info).getLevel() >= ((BaseSkill.BaseProperties) advancement.info.skill.getProperties()).getMaxLevel()) {
                    upgrade = false;
                }
            }
        }
        return upgrade;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0 && this.isUnlockable()) {
            Capabilities.get(this.mc.player).ifPresent(c -> {
                if (canUpgrade()) {
                    String description = "";
                    String title = this.isUnlocked()
                            ? TextHelper.translate("gui.advancement.confirm_upgrade")
                            : TextHelper.translate("gui.advancement.confirm_unlock");
                    if (advancement.info.skill instanceof ISkillAdvancement) {
                        ISkillAdvancement.Requirement requirement = ((ISkillAdvancement) advancement.info.skill).getRequirement(this.mc.player);
                        int levels = requirement.getLevels();
                        int xp = requirement.getXp();
                        Optional<SkillInfo> optional = c.getOwned(advancement.info.skill).filter(i -> i instanceof SkillInfo.IInfoUpgradeable);
                        if (optional.isPresent()) {
                            SkillInfo info = optional.get();
                            int lvl = ((SkillInfo.IInfoUpgradeable) info).getLevel();
                            description += TextHelper.translate("gui.advancement.description", lvl, lvl + 1);
                        }
                        if (levels > 0 || (xp > 0)) {
                            description += TextHelper.translate("gui.advancement.requires");
                        }
                        if (levels > 0) {
                            description += TextHelper.translate("gui.advancement.requires_levels", levels);
                        }
                        if (xp > 0) {
                            description += TextHelper.translate("gui.advancement.requires_xp", TextHelper.format2FloatPoint(xp));
                        }
                    }
                    description += TextHelper.translate("gui.advancement.undone_warning");
                    GuiScreenSkillAdvancements.confirmation = new GuiConfirmation(this.mc, title, description, (g) -> {
                        boolean success = this.advancement.upgrade();
                        if (g.isShifting && success) {
                            this.gui.gui.allowUserInput = false;
                            GuiScreenSkillAdvancements.onSkillUpgradeRunnable = () -> {
                                this.gui.gui.isShifting = true;
                                this.gui.gui.allowUserInput = true;
                                this.actionPerformed(button);
                            };
                        }
                    }, true, true, this.gui.gui.isShifting);
                    this.gui.gui.isShifting = false;
                    GuiScreenSkillAdvancements.confirmation.initGui();
                    button.playPressSound(this.mc.getSoundHandler());
                }
            });
        }
    }
}
