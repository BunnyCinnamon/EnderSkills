package arekkuusu.enderskills.common;

import arekkuusu.enderskills.api.ESAPI;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;

@Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/global")
public final class CommonConfig {

    @Config.Comment("Global Values")
    @Config.LangKey(LibMod.MOD_ID + ".config.global")
    public static Values CONFIG = new Values();
    @Config.Comment("Render Values")
    @Config.LangKey(LibMod.MOD_ID + ".config.render")
    public static RenderValues RENDER_CONFIG = new RenderValues();
    @Config.Ignore
    protected static Values CONFIG_SYNC = new Values();

    public static Values getValues() {
        return CONFIG;
    }

    public static Values getSyncValues() {
        return CONFIG_SYNC;
    }

    @Deprecated
    public static void initSyncConfig() {
        CommonConfig.getSyncValues().skill.defaultHumanTeam = CommonConfig.getValues().skill.defaultHumanTeam;
        ESAPI.defaultHumanTeam = CommonConfig.getSyncValues().skill.defaultHumanTeam;
        CommonConfig.getSyncValues().skill.globalCooldown = CommonConfig.getValues().skill.globalCooldown;
        CommonConfig.getSyncValues().skill.globalTime = CommonConfig.getValues().skill.globalTime;
        CommonConfig.getSyncValues().skill.globalRange = CommonConfig.getValues().skill.globalRange;
        CommonConfig.getSyncValues().skill.globalEffectiveness = CommonConfig.getValues().skill.globalEffectiveness;
        CommonConfig.getSyncValues().skill.extra.globalEffectEffectiveness = CommonConfig.getValues().skill.extra.globalEffectEffectiveness;
        CommonConfig.getSyncValues().advancement.oneTreePerClass = CommonConfig.getValues().advancement.oneTreePerClass;
        CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier = CommonConfig.getValues().advancement.xp.globalCostMultiplier;
        CommonConfig.getSyncValues().advancement.xp.retryXPReturn = CommonConfig.getValues().advancement.xp.retryXPReturn;
        CommonConfig.getSyncValues().advancement.maxRetries = CommonConfig.getValues().advancement.maxRetries;
        CommonConfig.getSyncValues().advancement.levels.tokenDiminishableCost = CommonConfig.getValues().advancement.levels.tokenDiminishableCost;
        CommonConfig.getSyncValues().advancement.levels.tokenCostThreshold = CommonConfig.getValues().advancement.levels.tokenCostThreshold;
        CommonConfig.getSyncValues().advancement.levels.defaultLevel = CommonConfig.getValues().advancement.levels.defaultLevel;
        CommonConfig.getSyncValues().worldGen.enderOreQuantity = CommonConfig.getValues().worldGen.enderOreQuantity;
        CommonConfig.getSyncValues().worldGen.enderOreSpawnRate = CommonConfig.getValues().worldGen.enderOreSpawnRate;
        CommonConfig.getSyncValues().worldGen.enderOreSpawnHeightMax = CommonConfig.getValues().worldGen.enderOreSpawnHeightMax;
        CommonConfig.getSyncValues().worldGen.enderOreSpawnHeightMin = CommonConfig.getValues().worldGen.enderOreSpawnHeightMin;
        CommonConfig.getSyncValues().worldGen.enderOreSpawnDimensions = CommonConfig.getValues().worldGen.enderOreSpawnDimensions;
        CommonConfig.getSyncValues().worldGen.enderOreItemDropsMin = CommonConfig.getValues().worldGen.enderOreItemDropsMin;
        CommonConfig.getSyncValues().worldGen.enderOreItemDropsMax = CommonConfig.getValues().worldGen.enderOreItemDropsMax;
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        for (Map.Entry<ResourceLocation, Skill> entry : registry.getEntries()) {
            entry.getValue().initSyncConfig();
        }
    }

    @Deprecated
    public static void writeSyncConfig(NBTTagCompound compound) {
        compound.setBoolean("advancement.oneTreePerClass", CommonConfig.getValues().advancement.oneTreePerClass);
        compound.setDouble("advancement.xp.globalCostMultiplier", CommonConfig.getValues().advancement.xp.globalCostMultiplier);
        compound.setDouble("advancement.xp.retryXPReturn", CommonConfig.getValues().advancement.xp.retryXPReturn);
        compound.setInteger("advancement.maxRetries", CommonConfig.getValues().advancement.maxRetries);
        compound.setDouble("advancement.levels.tokenDiminishableCost", CommonConfig.getValues().advancement.levels.tokenDiminishableCost);
        compound.setInteger("advancement.levels.tokenCostThreshold", CommonConfig.getValues().advancement.levels.tokenCostThreshold);
        compound.setInteger("advancement.levels.defaultLevel", CommonConfig.getValues().advancement.levels.defaultLevel);
        compound.setBoolean("defaultHumanTeam", CommonConfig.getValues().skill.defaultHumanTeam);
        compound.setDouble("globalCooldown", CommonConfig.getValues().skill.globalCooldown);
        compound.setDouble("globalTime", CommonConfig.getValues().skill.globalTime);
        compound.setDouble("globalRange", CommonConfig.getValues().skill.globalRange);
        compound.setDouble("globalEffectiveness", CommonConfig.getValues().skill.globalEffectiveness);
        compound.setDouble("extra.globalEffectEffectiveness", CommonConfig.getValues().skill.extra.globalEffectEffectiveness);
        ExpressionHelper.EXPRESSION_CACHE.clear();
        ExpressionHelper.FUNCTION_CACHE.clear();
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public static void readSyncConfig(NBTTagCompound compound) {
        CommonConfig.getSyncValues().advancement.oneTreePerClass = compound.getBoolean("advancement.xp.oneTreePerClass");
        CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier = compound.getDouble("advancement.xp.globalCostMultiplier");
        CommonConfig.getSyncValues().advancement.xp.retryXPReturn = compound.getDouble("advancement.xp.retryXPReturn");
        CommonConfig.getSyncValues().advancement.maxRetries = compound.getInteger("advancement.maxRetries");
        CommonConfig.getSyncValues().advancement.levels.tokenDiminishableCost = compound.getDouble("advancement.levels.tokenDiminishableCost");
        CommonConfig.getSyncValues().advancement.levels.tokenCostThreshold = compound.getInteger("advancement.levels.tokenCostThreshold");
        CommonConfig.getSyncValues().advancement.levels.defaultLevel = compound.getInteger("advancement.levels.defaultLevel");
        CommonConfig.getSyncValues().skill.defaultHumanTeam = compound.getBoolean("defaultHumanTeam");
        ESAPI.defaultHumanTeam = CommonConfig.getSyncValues().skill.defaultHumanTeam;
        CommonConfig.getSyncValues().skill.globalCooldown = compound.getDouble("globalCooldown");
        CommonConfig.getSyncValues().skill.globalTime = compound.getDouble("globalTime");
        CommonConfig.getSyncValues().skill.globalRange = compound.getDouble("globalRange");
        CommonConfig.getSyncValues().skill.globalEffectiveness = compound.getDouble("globalEffectiveness");
        CommonConfig.getSyncValues().skill.extra.globalEffectEffectiveness = compound.getDouble("extra.globalEffectEffectiveness");
        ExpressionHelper.EXPRESSION_CACHE.clear();
        ExpressionHelper.FUNCTION_CACHE.clear();
    }

    public static class Values {

        public final SkillGlobalConfig skill = new SkillGlobalConfig();
        public final SkillAdvancementConfig advancement = new SkillAdvancementConfig();
        public final WorldGen worldGen = new WorldGen();

        @Config.Comment("Syncs config reloads to all players in the server upon connecting or reloading server config files, if you are a dedicated server owner you might want this turned off")
        public boolean syncValuesToClient = true;

        public static class SkillGlobalConfig {

            public final Extra extra = new Extra();

            public boolean defaultHumanTeam = true;

            public double globalCooldown = 1D;

            public double globalTime = 1D;

            public double globalRange = 1D;

            public double globalEffectiveness = 1D;

            public static class Extra {
                public double globalEffectEffectiveness = 1D;
            }
        }

        public static class SkillAdvancementConfig {

            public final Experience xp = new Experience();
            public final Levels levels = new Levels();

            public boolean oneTreePerClass = true;

            public int maxRetries = 2;

            public static class Experience {
                public double globalCostMultiplier = 1D;
                public double retryXPReturn = 0.8;
            }

            public static class Levels {
                public double tokenDiminishableCost = 0.3;
                public int tokenCostThreshold = 10;
                public int defaultLevel = 1;
            }
        }

        public static class WorldGen {

            @Config.Comment("How many ore veins per chunk?")
            @Config.RangeInt(min = 0)
            public int enderOreSpawnRate = 11;

            @Config.Comment("How rare the ore is 0 to 1")
            @Config.RangeDouble(min = 0, max = 1)
            public double enderOreSpawnChance = 1;

            @Config.Comment("How many ore per spawn?")
            @Config.RangeInt(min = 0)
            public int enderOreQuantity = 4;

            @Config.Comment("Min spawn height?")
            @Config.RangeInt(min = 0)
            public int enderOreSpawnHeightMin = 0;

            @Config.Comment("Max spawn height?")
            @Config.RangeInt(min = 0)
            public int enderOreSpawnHeightMax = 32;

            @Config.Comment("Max spawn height?")
            @Config.RangeInt(min = 0)
            public int[] enderOreSpawnDimensions = {0};

            @Config.Comment("Min number of dust dropped?")
            @Config.RangeInt(min = 0)
            public int enderOreItemDropsMin = 1;

            @Config.Comment("Max number of dust dropped?")
            @Config.RangeInt(min = 0)
            public int enderOreItemDropsMax = 4;
        }
    }

    public static class RenderValues {
        public final SkillGroup skillGroup = new SkillGroup();
        public final Endurance endurance = new Endurance();
        public final Rendering rendering = new Rendering();

        public static class SkillGroup {
            public boolean renderUnowned = false;
            public boolean weightUnowned = true;
            public boolean renderOverlay = true;
            public boolean renderControls = true;
            public int overlayIcons = 6;
            public double scale = 1D;
            public int posX = 5;
            public int posY = 50;
            public int step = 17;
            public boolean inverse = false;
            public Orientation orientation = Orientation.VERTICAL;
        }

        public static class Endurance {
            public boolean renderOverlay = true;
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
        }

        public enum Orientation {
            VERTICAL,
            HORIZONTAL
        }
    }
}
