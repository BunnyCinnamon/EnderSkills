package arekkuusu.enderskills.client.gui;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.client.gui.widgets.GuiBaseButton;
import arekkuusu.enderskills.client.gui.widgets.GuiConfirmation;
import arekkuusu.enderskills.client.gui.widgets.GuiCustomButton;
import arekkuusu.enderskills.client.gui.widgets.SkillAdvancementTabType;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiScreenSkillAdvancements extends GuiScreen {

    public static final ResourceLocation WINDOW_0 = new ResourceLocation(LibMod.MOD_ID, "textures/gui/advancement/window_0.png");
    public static final ResourceLocation WINDOW_1 = new ResourceLocation(LibMod.MOD_ID, "textures/gui/advancement/window_1.png");
    public static final ResourceLocation WIDGETS = new ResourceLocation(LibMod.MOD_ID, "textures/gui/advancement/widgets.png");
    public static int tabPin = -1;
    public static int tabPagePin = -1;
    public static GuiConfirmation confirmation;

    public GuiSkillAdvancementTab selectedTab = null;
    public int tabPage = -1;
    public int maxPages = 0;

    public final NetworkPlayerInfo info = Minecraft.getMinecraft().getConnection().getPlayerInfo(Minecraft.getMinecraft().player.getUniqueID());
    public final GuiIngame inGameGui = Minecraft.getMinecraft().ingameGUI;
    public final List<GuiSkillAdvancementTab> tabs = Lists.newLinkedList();
    public List<GuiButton> alternateButtonList = new ArrayList<>();
    protected List<GuiLabel> alternateLabelList = Lists.<GuiLabel>newArrayList();
    public int guiWidth;
    public int guiHeight;
    public int x;
    public int y;
    public int minBoundaryX;
    public int maxBoundaryX;
    public int minBoundaryY;
    public int maxBoundaryY;
    public int scrollMouseX;
    public int scrollMouseY;
    public boolean isScrolling;

    public GuiScreenSkillAdvancements() {
        this.allowUserInput = true;
        this.guiWidth = 252;
        this.guiHeight = 169;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        alternateButtonList.clear();
        alternateLabelList.clear();
        this.x = (this.width - this.guiWidth) / 2;
        this.y = (this.height - this.guiHeight) / 2;
        Keyboard.enableRepeatEvents(false);
        minBoundaryX = x + 8;
        minBoundaryY = y + 7;
        maxBoundaryX = minBoundaryX + 236;
        maxBoundaryY = minBoundaryY + 154;
        maxPages = tabs.size();
        if (maxPages > 0) {
            buttonList.add(new GuiCustomButton(101, this.x + this.guiWidth - 13, this.y - 7, 15, 15, "", 36, 52, 15, 15, 0));
            buttonList.add(new GuiCustomButton(104, this.x - 3, this.y - 4, 10, 10, "", 51, 52, 10, 10, 0));

            buttonList.add(new GuiCustomButton(102, (this.x) + 2, (this.y - 12) + this.guiHeight, 18, 10, "", 18, 52, 18, 10, 0));
            buttonList.add(new GuiCustomButton(103, (this.x - 20) + this.guiWidth, (this.y - 12) + this.guiHeight, 18, 10, "", 0, 52, 18, 10, 0));
        }
        String respecTitle = "\u00A74Reset Unlocks\u00A7r";
        int respecWidth = this.mc.fontRenderer.getStringWidth(respecTitle) + 4;
        String storeTitle = "Store XP";
        int storeWidth = this.mc.fontRenderer.getStringWidth(storeTitle) + 4;
        String takeTitle = "Take XP";
        int takeWidth = this.mc.fontRenderer.getStringWidth(takeTitle) + 4;
        alternateButtonList.add(new GuiBaseButton(105, this.x + (this.guiWidth / 2) + (this.guiWidth / 4) - (respecWidth / 2), this.y + this.guiHeight - 50, respecWidth, 20, respecTitle));
        alternateButtonList.add(new GuiBaseButton(106, this.x + (this.guiWidth / 2) + (this.guiWidth / 4) - storeWidth - 2, this.y + this.guiHeight - 75, storeWidth, 20, storeTitle));
        alternateButtonList.add(new GuiBaseButton(107, this.x + (this.guiWidth / 2) + (this.guiWidth / 4) + 2, this.y + this.guiHeight - 75, takeWidth, 20, takeTitle));
        for (GuiSkillAdvancementTab tab : tabs) {
            tab.mc = this.mc;
            tab.initGui();
        }
        if (tabPin != -1) {
            this.tabPage = tabPin;
            this.selectedTab = tabs.get(this.tabPage);
            this.selectedTab.tabPage = tabPagePin;
            this.selectedTab.selectedPage = this.selectedTab.pages.get(this.selectedTab.tabPage);
        }

        if (confirmation != null) {
            confirmation.initGui();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiSkillAdvancementTab tab : tabs) {
            tab.updateScreen();
        }
        if (tabPage > -1 && tabPage < maxPages) {
            this.selectedTab = tabs.get(tabPage);
        } else {
            this.selectedTab = null;
        }
        for (GuiButton guiButton : alternateButtonList) {
            if (guiButton.id == 105) {
                guiButton.enabled = Capabilities.advancement(this.mc.player).map(c -> c.resetCount < CommonConfig.getValues().advancement.maxRetries).orElse(false);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Mouse.isButtonDown(0) && isPointInRegion(minBoundaryX, minBoundaryY, maxBoundaryX, maxBoundaryY, mouseX, mouseY)) {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.selectedPage.scroll(mouseX - this.scrollMouseX, mouseY - this.scrollMouseY);
            }

            this.scrollMouseX = mouseX;
            this.scrollMouseY = mouseY;
        } else {
            this.isScrolling = false;
        }

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        drawGuiBackground(mouseX, mouseY, partialTicks);
        drawGuiForeground(mouseX, mouseY, partialTicks);
        if (maxPages > 0) {
            for (GuiSkillAdvancementTab tab : this.tabs) {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                tab.drawTab(mouseX, mouseY, this.selectedTab == tab);
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
        }
        if (this.selectedTab != null) {
            super.drawScreen(mouseX, mouseY, partialTicks);
        } else {
            drawGuiLogo();
            for (GuiButton guiButton : this.alternateButtonList) {
                guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
            }
            for (GuiLabel label : this.alternateLabelList) {
                label.drawLabel(this.mc, mouseX, mouseY);
            }
        }
        if (confirmation == null) {
            int i = (this.width - this.guiWidth) / 2;
            int j = (this.height - this.guiHeight) / 2;
            this.renderToolTips(mouseX, mouseY, i, j, partialTicks);
        } else {
            confirmation.drawGui(mouseX, mouseY, partialTicks);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
    }

    public void drawGuiLogo() {
        this.mc.getTextureManager().bindTexture(WIDGETS);
        this.drawTexturedModalRect(this.x + this.guiWidth / 4 - 33, this.y + 15, 190, 0, 66, 61);
    }

    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {
        this.mc.getTextureManager().bindTexture(WINDOW_1);
        this.drawTexturedModalRect(this.x, this.y, 0, 0, this.guiWidth, this.guiHeight);
    }

    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {
        if (this.selectedTab != null) {
            //Draw Tab and Page Titles
            String title = this.selectedTab.title.getFormattedText() + " - " + (this.selectedTab.tabPage + 1) + "/" + this.selectedTab.maxPages;
            String subtitle = this.selectedTab.selectedPage.title.getFormattedText();
            int titleLength = this.mc.fontRenderer.getStringWidth(title);
            int subtitleLength = this.mc.fontRenderer.getStringWidth(subtitle);
            int length = Math.max(titleLength, subtitleLength);
            int xOffset = this.x;
            int yOffset = this.y - 27;
            int xOffsetTitleTexture = xOffset + ((this.guiWidth / 2) - (length + 20 + 20 + 10) / 2);
            int xOffsetSubtitleTexture = xOffset + ((this.guiWidth / 2) - (subtitleLength + 5 + 5) / 2);
            int xOffsetTitle = xOffset + ((this.guiWidth / 2) - (titleLength + 20 + 20 + 10) / 2);
            int xOffsetSubtitle = xOffset + ((this.guiWidth / 2) - (subtitleLength + 5 + 5) / 2);
            /*Title colors*/
            float r = (this.selectedTab.color >>> 16 & 0xFF) / 256F;
            float g = (this.selectedTab.color >>> 8 & 0xFF) / 256F;
            float b = (this.selectedTab.color & 0xFF) / 256F;
            /*Draw tab Subtitle*/
            GlStateManager.color(r, g, b, 1F);
            this.mc.getTextureManager().bindTexture(WIDGETS);
            this.drawTexturedModalRect(xOffsetSubtitleTexture - 1, yOffset, 66, 75, 5, 12);
            drawScaledCustomSizeModalRect(xOffsetSubtitleTexture + 4, yOffset, 72, 75, 1, 12, subtitleLength + 2, 12, 256, 256);
            this.drawTexturedModalRect(xOffsetSubtitleTexture + 6 + subtitleLength, yOffset, 74, 75, 5, 12);
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.drawString(this.mc.fontRenderer, subtitle, xOffsetSubtitle + 5, yOffset + 2, 0xC4C4C4);
            //Draw tab Title
            GlStateManager.color(r, g, b, 1F);
            this.mc.getTextureManager().bindTexture(WIDGETS);
            this.drawTexturedModalRect(xOffsetTitleTexture, yOffset - 14, 66, 52, 20, 23);
            drawScaledCustomSizeModalRect(xOffsetTitleTexture + 20, yOffset - 14, 87, 52, 1, 23, length + 10, 23, 256, 256);
            this.drawTexturedModalRect(xOffsetTitleTexture + 20 + length + 10, yOffset - 14, 89, 52, 20, 23);
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.drawString(this.mc.fontRenderer, title, xOffsetTitle + 20 + 5, yOffset - 11, -1);
            //Draw tab GUI
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            this.selectedTab.selectedPage.drawGuiBackground(mouseX, mouseY, partialTicks);
            this.selectedTab.selectedPage.drawGuiForeground(mouseX, mouseY, partialTicks);
            GlStateManager.disableDepth();
            GlStateManager.depthFunc(515);
            GlStateManager.popMatrix();
        }
        //Draw XP on book
        Capabilities.advancement(this.mc.player).ifPresent(c -> {
            int xpValue = c.getExperienceTotal(this.mc.player);
            int xpTotal = XPHelper.getLevelFromXPValue(xpValue);
            double xpCurrent = XPHelper.getLevelProgressFromXPValue(xpValue);

            this.mc.getTextureManager().bindTexture(WIDGETS);
            drawXPBar(xpCurrent, 25, -13, 52);
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + 25 + 24 + 10 * 16, this.y - 12, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.drawString(this.mc.fontRenderer, xpValue + " XP", 0, 0, -1);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();

            int unlocks = c.level;
            double unlocksCurrent = c.levelProgress;

            this.mc.getTextureManager().bindTexture(WIDGETS);
            drawXPBar(unlocksCurrent, 25, -6, 62);
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + 25 + 24 + 10 * 16, this.y - 5, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.drawString(this.mc.fontRenderer, unlocks + " ender level(s)", 0, 0, -1);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            String xpTotalString = String.valueOf(xpTotal);
            int lenght = fontRenderer.getStringWidth(xpTotalString);
            GlStateManager.translate(this.x + 25 + 24 - (lenght / 2D) + 10 * 8, this.y - 13, 0);
            GlStateManager.scale(0.7F, 0.7F, 0.7F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.drawString(this.mc.fontRenderer, xpTotalString, 0, 0, -1);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();
        });
        //Draw Border
        this.mc.getTextureManager().bindTexture(WINDOW_0);
        this.drawTexturedModalRect(this.x, this.y, 0, 0, this.guiWidth, this.guiHeight);
        //Draw Player Info of Main GUI
        if (this.selectedTab == null) {
            this.mc.getTextureManager().bindTexture(WIDGETS);
            RenderMisc.draw8Rect(this.x + 8 + 10, this.y + 80, (this.guiWidth / 2) - 35, 68);
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + (float) (this.guiWidth / 4) - 18, this.y + 142, 0);
            //Render Entity Player
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawEntityOnScreen(0, 0, this.x + 45 - mouseX, this.y + 93 - mouseY);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            //Render Stats
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + (float) (this.guiWidth / 4) + 5, this.y + 85, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            drawString(this.mc.fontRenderer, this.mc.player.getDisplayName().getFormattedText(), 0, 0, -1);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + (float) (this.guiWidth / 4) + 5, this.y + 90, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            int offset = fontRenderer.FONT_HEIGHT;
            int row = 0;
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.MAX_HEALTH, "ui." + LibMod.MOD_ID + ".max_health.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.ATTACK_DAMAGE, "ui." + LibMod.MOD_ID + ".attack_damage.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.ATTACK_SPEED, "ui." + LibMod.MOD_ID + ".attack_speed.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.ARMOR, "ui." + LibMod.MOD_ID + ".armor.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.ARMOR_TOUGHNESS, "ui." + LibMod.MOD_ID + ".armor_toughness.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.MOVEMENT_SPEED, "ui." + LibMod.MOD_ID + ".move_speed.info", offset * row++);
            this.drawPlayerAttributeInfo(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "ui." + LibMod.MOD_ID + ".knockback_resistance.info", offset * row);
            GlStateManager.popMatrix();
        }
    }

    public void drawXPBar(double percentage, int x, int y, int v) {
        this.drawTexturedModalRect(this.x + x, this.y + y, 109, v, 10, 5);
        for (int i = 0; i < 16; i++) {
            this.drawTexturedModalRect(this.x + x + 10 + i * 10, this.y + y, 119, v, 10, 5);
        }
        this.drawTexturedModalRect(this.x + x + 9 + 10 * 16, this.y + y, 129, v, 11, 5);
        if (percentage > 0) {
            double w0 = 1D / 18D; //% to fill for one cell
            double w = w0; //% to fill for all cells up to target cell, we begin at cell 1, then 2-17, and 18
            this.drawTexturedModalRect(this.x + x, this.y + y, 109, v + 5, percentage > w ? 10 : (int) (10D * ((w - percentage) / w0)), 5);
            for (int i = 0; i < 16; i++) {
                w = (1D + i) / 18D;
                this.drawTexturedModalRect(this.x + x + 10 + i * 10, this.y + y, 119, v + 5, percentage < w ? 0 : (int) (10D * ((percentage - w) / w0)), 5);
            }
            w = 17D / 18D;
            this.drawTexturedModalRect(this.x + x + 9 + 10 * 16, this.y + y, 129, v + 5, percentage < w ? 0 : (int) (11D * ((percentage - w) / w0)), 5);
        }
    }

    public void drawEntityOnScreen(int posX, int posY, float mouseX, float mouseY) {
        if (confirmation != null) return;
        EntityLivingBase ent = this.mc.player;
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale((-30), 30, 30);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
    }

    public void drawPlayerAttributeInfo(IAttribute attribute, String translateKey, int offset) {
        IAttributeInstance playerAttribute = this.mc.player.getEntityAttribute(attribute);
        String value = TextHelper.format2FloatPoint(playerAttribute.getAttributeValue());
        String translatedKey = I18n.format(translateKey, value);
        drawString(mc.fontRenderer, translatedKey, 0, offset, -0x2D2D2D);
    }

    public void renderToolTips(int mouseX, int mouseY, int x, int y, float partialTicks) {
        if (maxPages > 0) {
            if (this.selectedTab != null) {
                this.selectedTab.selectedPage.drawToolTips(mouseX, mouseY, partialTicks);
            }
            for (GuiSkillAdvancementTab tab : this.tabs) {
                if (tab.isMouseOver(x, y, mouseX, mouseY)) {
                    this.drawHoveringText(tab.title.getFormattedText(), mouseX, mouseY);
                }
            }
        }
    }

    public boolean isPointInRegion(int rectXMin, int rectYMin, int rectXMax, int rectYMax, int mouseX, int mouseY) {
        return mouseX >= rectXMin && mouseY >= rectYMin && mouseX < rectXMax && mouseY < rectYMax;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (confirmation == null) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            if (mouseButton == 0) {
                int width = (this.width - this.guiWidth) / 2;
                int height = (this.height - this.guiHeight) / 2;
                for (GuiSkillAdvancementTab tab : this.tabs) {
                    if (tab.isMouseOver(width, height, mouseX, mouseY)) {
                        if (tabPage != tabs.indexOf(tab)) {
                            tabPage = tabs.indexOf(tab);
                            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ModSounds.PAGE_TURN, 2.5F));
                        }
                        break;
                    }
                }

                if (selectedTab != null) {
                    selectedTab.mouseClicked(mouseX, mouseY, mouseButton);
                } else {
                    for (int i = 0; i < this.alternateButtonList.size(); ++i) {
                        GuiButton guibutton = this.alternateButtonList.get(i);

                        if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                            net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.alternateButtonList);
                            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                                break;
                            guibutton = event.getButton();
                            this.selectedButton = guibutton;
                            guibutton.playPressSound(this.mc.getSoundHandler());
                            this.actionPerformed(guibutton);
                            if (this.equals(this.mc.currentScreen))
                                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.alternateButtonList));
                        }
                    }
                }
            }
        } else {
            confirmation.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            if (confirmation == null) {
                this.mc.displayGuiScreen(null);

                if (this.mc.currentScreen == null) {
                    this.mc.setIngameFocus();
                }
            } else {
                confirmation = null;
            }
        } else if (keyCode == 203) {
            this.selectedTab.tabPage = Math.max(this.selectedTab.tabPage - 1, 0);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        } else if (keyCode == 205) {
            this.selectedTab.tabPage = Math.min(this.selectedTab.tabPage + 1, this.selectedTab.maxPages - 1);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 101) {
            tabPage = -1;
        }
        if (this.selectedTab != null) {
            if (button.id == 102) {
                this.selectedTab.tabPage = Math.max(this.selectedTab.tabPage - 1, 0);
            } else if (button.id == 103) {
                this.selectedTab.tabPage = Math.min(this.selectedTab.tabPage + 1, this.selectedTab.maxPages - 1);
            } else if (button.id == 104) {
                if (this.tabs.indexOf(this.selectedTab) != tabPin || this.selectedTab.tabPage != tabPagePin) {
                    tabPin = this.tabs.indexOf(this.selectedTab);
                    tabPagePin = this.selectedTab.tabPage;
                } else {
                    tabPin = -1;
                    tabPagePin = -1;
                }
            }
        } else {
            if (button.id == 105) {
                GuiScreenSkillAdvancements.confirmation = new GuiConfirmation(this.mc, "Confirm Reset of all progress?", "\n\u00A74This action cannot be undone.\u00A7r", () -> {
                    PacketHelper.sendResetSkillsRequestPacket(this.mc.player);
                }, true, true);
                GuiScreenSkillAdvancements.confirmation.initGui();
            } else if (button.id == 106) {
                PacketHelper.sendStoreXPRequestPacket(this.mc.player);
            } else if (button.id == 107) {
                PacketHelper.sendTakeXPRequestPacket(this.mc.player);
            }
        }
    }

    @Nullable
    public GuiSkillAdvancementTab addTab(ITextComponent title, SkillAdvancementTabType type, int color, int page) {
        int typeMaxAmount = type.getMax();
        int typeAmount = (int) this.tabs.stream().filter(a -> a.type == type).count();
        if (typeMaxAmount > typeAmount) {
            GuiSkillAdvancementTab tab = new GuiSkillAdvancementTab(this, type, title, color, page);
            this.tabs.add(tab);
            return tab;
        }
        return null;
    }
}
