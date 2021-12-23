package arekkuusu.enderskills.client;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.world.BossInfo;
import net.minecraftforge.common.config.Config;

@Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/client")
public final class ClientConfig {

    @Config.LangKey(LibMod.MOD_ID + ".config.render")
    public static RenderValues RENDER_CONFIG = new RenderValues();

    public static class RenderValues {
        public final SkillGroup skillGroup = new SkillGroup();
        public final Endurance endurance = new Endurance();
        public final Rendering rendering = new Rendering();

        public static class SkillGroup {
            public boolean renderUnowned = false;
            public boolean renderOverlay = true;
            public boolean renderControls = true;
            public boolean renderTitle = true;
            public boolean renderDenominator = true;
            public double scale = 1D;
            public int posX = 5;
            public int posY = 50;
            public int step = 17;
            public boolean inverse = false;
            public Orientation orientation = Orientation.VERTICAL;
        }

        public static class Endurance {
            public boolean renderOverlay = true;
            public boolean renderTitle = true;
            public double scale = 1D;
            public int size = 182;
            public int posX = 5;
            public int posY = 5;
            public BossInfo.Color color = BossInfo.Color.BLUE;
            public BossInfo.Overlay overlay = BossInfo.Overlay.NOTCHED_20;
            public Orientation orientation = Orientation.HORIZONTAL;
        }

        public static class Rendering {
            @Config.Comment("Use vanilla render effects on the more fancy renders (for low end graphics cards)")
            public boolean vanilla = false;
            @Config.Comment("Check this if even the vanilla renders are killing your fps")
            public boolean helpMyFramesAreDying = false;
            @Config.Comment("Check this if shaders are broken to turn them off")
            public boolean helpMyShadersAreDying = false;
            @Config.Comment("Particle Spawning Chance")
            @Config.RangeDouble(min = 0, max = 1)
            @Config.SlidingOption
            public double particleSpawning = 1D;
        }

        public enum Orientation {
            VERTICAL,
            HORIZONTAL
        }
    }
}
