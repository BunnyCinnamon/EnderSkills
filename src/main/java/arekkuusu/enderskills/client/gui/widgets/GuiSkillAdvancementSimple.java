package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.util.Pair;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.gui.data.SkillAdvancement;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionSimple;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementInfo;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.handler.GuiHandler;
import arekkuusu.enderskills.common.skill.ModAttributes;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.advancements.AdvancementState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class GuiSkillAdvancementSimple extends GuiSkillAdvancement {

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
            if (skillInfo instanceof InfoUpgradeable) {
                int skillTier = ((InfoUpgradeable) skillInfo).getLevel();
                int topLevel = getTopLevel();
                if (skillTier > topLevel && topLevel > 0) {
                    String tier = String.valueOf(topLevel);
                    String plus = "+" + (skillTier - topLevel);
                    drawString(mc.fontRenderer, tier, this.xOffset + 18 + 10 - mc.fontRenderer.getStringWidth(tier) - mc.fontRenderer.getStringWidth(plus),
                            this.yOffset + 18 + mc.fontRenderer.FONT_HEIGHT / 2, -1);
                    drawString(mc.fontRenderer, plus, this.xOffset + 18 + 10 - mc.fontRenderer.getStringWidth(plus),
                            this.yOffset + 18 + mc.fontRenderer.FONT_HEIGHT / 2, 0xFFDB32);
                } else if (skillTier > 0) {
                    String tier = String.valueOf(skillTier);
                    drawString(mc.fontRenderer, tier, this.xOffset + 18 + 10 - mc.fontRenderer.getStringWidth(tier),
                            this.yOffset + 18 + mc.fontRenderer.FONT_HEIGHT / 2, -1);
                }
            }
        });
    }

    public int getTopLevel() {
        return DSLEvaluator.evaluateMaxLevel(advancement.info.skill);
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
        final String title = this.advancement.info.title.getFormattedText();
        final String description = this.advancement.info.description.getFormattedText();
        final List<String> descriptionLines = Lists.newArrayList(description.split("\n"));

        this.fillInDescription(descriptionLines);

        List<String> descriptionFormattedLines = new ArrayList<>();
        int titleWidth = mc.fontRenderer.getStringWidth(title);
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
        if (recalculateWidth) {
            titleWidth = mc.fontRenderer.getStringWidth(title);

            for (String descriptionLine : descriptionFormattedLines) {
                int s;
                if (titleWidth < (s = this.mc.fontRenderer.getStringWidth(descriptionLine) - 25)) {
                    titleWidth = s;
                }
            }
        }
        int descriptionHeight = 32 + descriptionFormattedLines.size() * this.mc.fontRenderer.FONT_HEIGHT;
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
            for (int i = 0; i < descriptionFormattedLines.size(); ++i) {
                GlStateManager.color(1F, 1F, 1F, 1F);
                this.mc.fontRenderer.drawString(descriptionFormattedLines.get(i), (float) (fitXOffset + 5), (float) (this.yOffset + 9 + 17 + i * this.mc.fontRenderer.FONT_HEIGHT), -5592406, false);
                GlStateManager.color(1F, 1F, 1F, 1F);
            }
        } else {
            for (int i = 0; i < descriptionFormattedLines.size(); ++i) {
                GlStateManager.color(1F, 1F, 1F, 1F);
                this.mc.fontRenderer.drawString(descriptionFormattedLines.get(i), (float) (fitXOffset + 5), (float) (this.yOffset + 26 - descriptionHeight + 7 + i * this.mc.fontRenderer.FONT_HEIGHT), -5592406, false);
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

    private void fillInDescription(List<String> descriptionLines) {
        Capabilities.get(Minecraft.getMinecraft().player).flatMap(skilledEntityCapability -> skilledEntityCapability.getOwned(this.advancement.info.skill)).ifPresent(skillInfo -> {
            InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
            int level = infoUpgradeable.getLevel();
            int maxLevel = DSLEvaluator.evaluateMaxLevel(this.advancement.info.skill);
            if (!GuiScreen.isShiftKeyDown()) {
                descriptionLines.add("");
                descriptionLines.add(TextHelper.translate("desc.stats.shift"));
            } else {
                ResourceLocation registryName = this.advancement.info.skill.getRegistryName();
                DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(registryName);
                descriptionLines.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this.advancement.info.skill, level))));
                descriptionLines.add("");
                //
                //
                if (level < maxLevel) {
                    if (!GuiScreen.isCtrlKeyDown()) {
                        descriptionLines.add(TextHelper.translate("desc.stats.level_current", level, level));
                        fillStatsWithLevel(descriptionLines, level, registryName, config);
                        descriptionLines.add("");
                        descriptionLines.add(TextHelper.translate("desc.stats.ctrl"));
                    } else {
                        descriptionLines.add(TextHelper.translate("desc.stats.level_next", level, level + 1));
                        fillStatsWithLevel(descriptionLines, level + 1, registryName, config);
                    }
                } else {
                    descriptionLines.add(TextHelper.translate("desc.stats.level_max", maxLevel));
                    fillStatsWithLevel(descriptionLines, level, registryName, config);
                }
            }
        });
    }

    private void fillStatsWithLevel(List<String> descriptionLines, int level, ResourceLocation registryName, DSLConfig config) {
        for (String entry : config.map.keySet()) {
            if (entry.equalsIgnoreCase("xp")) continue;
            if (entry.equalsIgnoreCase("endurance")) continue;
            if (entry.equalsIgnoreCase("range_extra")) continue;
            double value;
            if (GuiHandler.SPECIFIC_VALUES.containsKey(new Pair<>(registryName, entry))) {
                value = GuiHandler.SPECIFIC_VALUES.get(new Pair<>(registryName, entry)).apply(this.advancement.info.skill, level).doubleValue();
            } else {
                value = GuiHandler.VALUES.get(entry).apply(this.advancement.info.skill, level).doubleValue();
            }
            String suffix;
            if (GuiHandler.SPECIFIC_SUFFIX.containsKey(new Pair<>(registryName, entry))) {
                suffix = GuiHandler.SPECIFIC_SUFFIX.get(new Pair<>(registryName, entry));
            } else {
                suffix = GuiHandler.SUFFIX.get(entry).toLowerCase(Locale.ROOT);
            }
            String text = TextHelper.translate("desc.stats." + entry.toLowerCase(Locale.ROOT), TextHelper.format2FloatPoint(value), TextHelper.getTextComponent("desc.stats." + suffix));
            if (config.map.containsKey("RANGE_EXTRA") && entry.equalsIgnoreCase("RANGE")) {
                double value1;
                if (GuiHandler.SPECIFIC_VALUES.containsKey(new Pair<>(registryName, "RANGE_EXTRA"))) {
                    value1 = GuiHandler.SPECIFIC_VALUES.get(new Pair<>(registryName, "RANGE_EXTRA")).apply(this.advancement.info.skill, level).doubleValue();
                } else {
                    value1 = GuiHandler.VALUES.get("RANGE_EXTRA").apply(this.advancement.info.skill, level).doubleValue();
                }
                String suffix1;
                if (GuiHandler.SPECIFIC_SUFFIX.containsKey(new Pair<>(registryName, "RANGE_EXTRA"))) {
                    suffix1 = GuiHandler.SPECIFIC_SUFFIX.get(new Pair<>(registryName, "RANGE_EXTRA"));
                } else {
                    suffix1 = GuiHandler.SUFFIX.get("RANGE_EXTRA").toLowerCase(Locale.ROOT);
                }
                text += TextHelper.translate("desc.stats.range_extra", TextHelper.format2FloatPoint(value1), TextHelper.getTextComponent("desc.stats." + suffix1));
            }
            descriptionLines.add(text);
        }
    }

    public boolean canUpgrade() {
        boolean upgrade = true;
        SkilledEntityCapability c = Capabilities.get(this.mc.player).orElse(null);
        if (c != null && c.isOwned(advancement.info.skill)) {
            SkillInfo info = c.getOwned(advancement.info.skill).orElse(null);
            if (info instanceof InfoUpgradeable) {
                if (((InfoUpgradeable) info).getLevel() >= getTopLevel()) {
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
                    if (GuiHandler.ADVANCEMENTS.containsKey(this.advancement.info.skill.getRegistryName())) {
                        SkillAdvancement.Requirement requirement = GuiHandler.ADVANCEMENTS.get(this.advancement.info.skill.getRegistryName()).getRequirement(this.mc.player);
                        int levels = requirement.getLevels();
                        int xp = requirement.getXp();
                        Optional<SkillInfo> optional = c.getOwned(advancement.info.skill).filter(i -> i instanceof InfoUpgradeable);
                        if (optional.isPresent()) {
                            SkillInfo info = optional.get();
                            int lvl = ((InfoUpgradeable) info).getLevel();
                            description += TextHelper.translate("gui.advancement.description");
                            description += TextHelper.translate("gui.advancement.description_levels", lvl, lvl + 1);
                        }
                        description += TextHelper.translate("gui.advancement.requires");
                        if (levels > 0) {
                            description += TextHelper.translate("gui.advancement.requires_levels", levels);
                        }
                        if (xp != 0) {
                            description += TextHelper.translate("gui.advancement.requires_xp", TextHelper.format2FloatPoint(xp));
                        }
                    }
                    description += TextHelper.translate("gui.advancement.undone_warning");
                    GuiScreenSkillAdvancements.confirmation = new GuiConfirmation(this.mc, this.gui.gui, title, description, (g) -> {
                        boolean success = this.advancement.upgrade();
                        if (g.isShifting && success) {
                            this.gui.gui.allowUserInput = false;
                            GuiScreenSkillAdvancements.onSkillUpgradeRunnable = () -> {
                                this.gui.gui.isShifting = g.isShifting;
                                this.gui.gui.allowUserInput = true;
                                this.actionPerformed(button);
                            };
                        }
                    }, true, true, this.gui.gui.isShifting);
                    GuiScreenSkillAdvancements.confirmation.initGui();
                    button.playPressSound(this.mc.getSoundHandler());
                }
            });
        }
    }
}
