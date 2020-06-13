package arekkuusu.enderskills.client.gui.widgets;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

public enum SkillAdvancementTabType {
    ABOVE(0, 0, 28, 32, 8),
    BELOW(84, 0, 28, 32, 8),
    LEFT(0, 65, 32, 28, 5),
    RIGHT(96, 65, 32, 28, 5);

    public static final int MAX_TABS;
    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;
    private final int max;

    SkillAdvancementTabType(int textureX, int textureY, int widthIn, int heightIn, int max) {
        this.textureX = textureX;
        this.textureY = textureY;
        this.width = widthIn;
        this.height = heightIn;
        this.max = max;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(Gui guiIn, int x, int y, boolean selected, int index, int color) {
        long seed = 0;
        seed += index + color;
        Random random = new Random(seed);
        int variation = random.nextInt(2);
        int i = this.textureX + this.width * variation;

        float r = (color >>> 16 & 0xFF) / 256F;
        float g = (color >>> 8 & 0xFF) / 256F;
        float b = (color & 0xFF) / 256F;
        int j = selected ? this.textureY + this.height : this.textureY;
        GlStateManager.color(r, g, b, 1F);
        guiIn.drawTexturedModalRect(x + this.getX(index), y + this.getY(index), i, j, this.width, this.height);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public int getX(int index) {
        switch (this) {
            case ABOVE:
                return 7 + this.width * index;
            case BELOW:
                return 14 + this.width * index;
            case LEFT:
                return -this.width + 10;
            case RIGHT:
                return 242;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getY(int index) {
        switch (this) {
            case ABOVE:
                return -this.height + 9;
            case BELOW:
                return 159;
            case LEFT:
            case RIGHT:
                return 7 + this.height * index;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public boolean isMouseOver(int p_192654_1_, int p_192654_2_, int p_192654_3_, int p_192654_4_, int p_192654_5_) {
        int i = p_192654_1_ + this.getX(p_192654_3_);
        int j = p_192654_2_ + this.getY(p_192654_3_);
        return p_192654_4_ > i && p_192654_4_ < i + this.width && p_192654_5_ > j && p_192654_5_ < j + this.height;
    }

    static {
        int i = 0;

        for (SkillAdvancementTabType tab : values()) {
            i += tab.max;
        }

        MAX_TABS = i;
    }
}
