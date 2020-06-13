package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.client.gui.GuiSkillAdvancementPage;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import java.util.List;
import java.util.regex.Pattern;

public class GuiSkillAdvancement extends Gui {

    public static final Pattern PATTERN = Pattern.compile("(.+) \\S+");
    public final List<GuiButton> buttonList = Lists.newArrayList();
    public final GuiSkillAdvancementPage gui;
    public List<GuiSkillAdvancement> children = Lists.newArrayList();
    public List<GuiSkillAdvancement> parents = Lists.newArrayList();
    public Minecraft mc;
    public int x;
    public int y;
    public int xOffset;
    public int yOffset;

    public GuiSkillAdvancement(GuiSkillAdvancementPage gui) {
        this.gui = gui;
    }

    public void initGui() {

    }

    public void updateScreen() {

    }

    public void drawGui(int mouseX, int mouseY, float partialTicks) {
        this.xOffset = x + gui.scrollX;
        this.yOffset = y + gui.scrollY;
        drawGuiBackground(mouseX, mouseY, partialTicks);
        drawGuiForeground(mouseX, mouseY, partialTicks);
    }

    public void drawConnectivity(boolean outline) {
        for (GuiSkillAdvancement parent : parents) {
            drawConnectivity(parent.getConnectivityX(), parent.yOffset, this.getConnectivityX(), this.yOffset, outline);
        }
    }

    public void drawConnectivity(int xFrom, int yFrom, int xTo, int yTo, boolean outline) {
        int parentYStart = yFrom + 13;
        int parentYOffset = yFrom + (26 + 4);
        int parentX = xFrom;
        int childYStart = yTo + 13;
        int childYOffset = yTo - 7;
        int childX = xTo;
        int color = outline ? 0x000000FF : 0xFFFFFFFF;

        if (outline) {
            this.drawVerticalLine(parentX - 1, parentYStart + 1, parentYOffset - 1, color);
            this.drawVerticalLine(parentX, parentYStart + 1, parentYOffset - 1, color);
            this.drawVerticalLine(parentX + 1, parentYStart + 1, parentYOffset - 1, color);
            this.drawHorizontalLine(parentX, childX, parentYOffset + 1, color);
            this.drawHorizontalLine(parentX, childX, parentYOffset + 1, color);
            this.drawVerticalLine(childX - 1, childYStart + 1, childYOffset - 1, color);
            this.drawVerticalLine(childX, childYStart + 1, childYOffset - 1, color);
            this.drawVerticalLine(childX + 1, childYStart + 1, childYOffset - 1, color);
        } else {
            this.drawVerticalLine(parentX, parentYStart, parentYOffset, color);
            this.drawHorizontalLine(parentX, childX, parentYOffset, color);
            this.drawVerticalLine(childX, childYStart, childYOffset, color);
        }
    }

    public int getConnectivityX() {
        return this.xOffset + 13;
    }

    public void drawGuiBackground(int mouseX, int mouseY, float partialTicks) {

    }

    public void drawGuiForeground(int mouseX, int mouseY, float partialTicks) {

    }

    public void drawSkillToolTip(int mouseX, int mouseY, float partialTicks) {

    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (GuiButton guibutton : this.buttonList) {
                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    //guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                }
            }
        }
    }

    public void actionPerformed(GuiButton button) {
        //For-Rent
    }

    @Override
    public void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        RenderMisc.drawRect(startX, y, endX + 1, y + 1, color);
    }

    @Override
    public void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        RenderMisc.drawRect(x, startY + 1, x + 1, endY, color);
    }

    public boolean isHovered() {
        return false;
    }

    public boolean isVisible() {
        return false;
    }

    public boolean isUnlocked() {
        return true;
    }

    public boolean isUnlockable() {
        return false;
    }

    public void addChildren(GuiSkillAdvancement advancement) {
        this.children.add(advancement);
        advancement.parents.add(this);
    }

    public void addParent(GuiSkillAdvancement advancement) {
        this.parents.add(advancement);
        advancement.children.add(this);
    }
}
