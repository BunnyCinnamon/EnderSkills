package arekkuusu.enderskills.client.gui.data;

import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SkillAdvancementInfo {

    public final ITextComponent title;
    public final ITextComponent description;
    public final Frame frame;
    public final Skill skill;
    public final boolean hidden;

    public SkillAdvancementInfo(ITextComponent title, ITextComponent description, Frame frame, Skill skill, boolean hidden) {
        this.title = title;
        this.description = description;
        this.frame = frame;
        this.skill = skill;
        this.hidden = hidden;
    }

    public enum Frame {
        NONE("none", 0, TextFormatting.WHITE),
        NORMAL("normal", 0, TextFormatting.GREEN),
        ROUNDED("rounded", 26, TextFormatting.DARK_PURPLE),
        SPECIAL("special", 52, TextFormatting.GREEN);

        public final TextFormatting format;
        public final String name;
        public final int icon;

        Frame(String nameIn, int iconIn, TextFormatting formatIn) {
            this.name = nameIn;
            this.icon = iconIn;
            this.format = formatIn;
        }

        public String getName() {
            return this.name;
        }

        public static Frame byName(String nameIn) {
            Frame frame = NORMAL;
            for (Frame frametype : values()) {
                if (frametype.name.equals(nameIn)) {
                    frame = frametype;
                    break;
                }
            }
            return frame;
        }

        @SideOnly(Side.CLIENT)
        public int getIcon() {
            return this.icon;
        }

        public TextFormatting getFormat() {
            return this.format;
        }
    }
}
