package arekkuusu.enderskills.client.gui;

import arekkuusu.enderskills.client.gui.widgets.SkillAdvancementTabType;
import arekkuusu.enderskills.common.lib.LibMod;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiSkillAdvancementTab extends Gui {

    public static final ResourceLocation TABS = new ResourceLocation(LibMod.MOD_ID, "textures/gui/advancement/tabs.png");
    public final List<GuiSkillAdvancementPage> pages = Lists.newLinkedList();
    public final GuiScreenSkillAdvancements gui;
    public final SkillAdvancementTabType type;
    public final ITextComponent title;
    public final int color;
    public final int index;
    public GuiSkillAdvancementPage selectedPage;
    public int maxPages = 0;
    public int tabPage = 0;
    public Minecraft mc;
    public int x;
    public int y;
    public boolean firstOpened;

    public GuiSkillAdvancementTab(GuiScreenSkillAdvancements gui, SkillAdvancementTabType type, ITextComponent title, int color, int index) {
        this.gui = gui;
        this.type = type;
        this.title = title;
        this.color = color;
        this.index = index;
    }

    public void initGui() {
        this.x = gui.x;
        this.y = gui.y;
        this.maxPages = pages.size();
        for (GuiSkillAdvancementPage page : pages) {
            page.mc = this.mc;
            page.initGui();
        }
        if (!firstOpened) {
            firstOpened = true;
            for (GuiSkillAdvancementPage page : pages) {
                if (!page.advancements.isEmpty() && page.advancements.get(0).isUnlocked()) {
                    tabPage = pages.indexOf(page);
                }
            }
        }
    }

    public void updateScreen() {
        if (tabPage > -1 && tabPage < maxPages) {
            selectedPage = pages.get(tabPage);
        }
        selectedPage.updateScreen();
    }

    public void drawTab(int mouseX, int mouseY, boolean selected) {
        this.mc.getTextureManager().bindTexture(TABS);
        this.type.draw(this, this.x, this.y, selected, this.index, this.color);
    }

    public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
        return this.type.isMouseOver(x, y, this.index, mouseX, mouseY);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        selectedPage.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public GuiSkillAdvancementPage addPage(ITextComponent title) {
        GuiSkillAdvancementPage tab = new GuiSkillAdvancementPage(gui, this, title);
        this.pages.add(tab);
        return tab;
    }
}
