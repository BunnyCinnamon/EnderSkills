package arekkuusu.enderskills.common;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraftforge.common.config.Config;

@Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/global")
public final class CommonConfig {

    @Config.Ignore
    public static CommonConfig.Values CONFIG_SYNC = new CommonConfig.Values();
    public static CommonConfig.Values CONFIG = new CommonConfig.Values();

    public static Values getSyncValues() {
        return CONFIG_SYNC;
    }

    public static Values getConfig() {
        return CONFIG;
    }

    public static class Values {

        public final SkillGlobalConfig skill = new SkillGlobalConfig();
        public final SkillAdvancementConfig advancement = new SkillAdvancementConfig();
        public final WorldGen worldGen = new WorldGen();
        public final WorldDrops worldDrops = new WorldDrops();

        @Config.Comment("Syncs config reloads to all players in the server upon connecting or reloading server config files, if you are a dedicated server owner you might want this turned off")
        public boolean syncValuesToClient = true;

        public static class SkillGlobalConfig {

            @Config.Comment("Disallows abilities from breaking blocks")
            public boolean destroyBlocks = false;

            @Config.Comment("Disallows DoT abilities from knockback")
            public boolean preventAbilityDoTKnockback = true;

            @Config.Comment("Disallows abilities to target humans")
            public boolean defaultHumanTeam = true;

            @Config.Comment("Disallows abilities to target passive mobs")
            public boolean defaultAnimalTeam = false;

            @Config.Comment("Modifies negative effects of all abilities (used for balancing heals)")
            public double globalPositiveEffect = 1D;

            @Config.Comment("Modifies positive effects of all abilities (used for balancing damage)")
            public double globalNegativeEffect = 1.5D;

            @Config.Comment("Modifies regeneration delay after using an ability in minecraft seconds (20 = one second)")
            public int enduranceDelay = 5 * 20;

            @Config.Comment("Modifies regeneration speed in minecraft seconds (20 = one second)")
            public int enduranceRegen = 10;
        }

        public static class SkillAdvancementConfig {

            public final Experience xp = new Experience();
            public final Levels levels = new Levels();

            public boolean oneTreePerClass = true;

            public int maxResetUnlocks = 2;

            public static class Experience {
                @Config.Comment("Cost increment of all advancements")
                public double globalCostMultiplier = 0.5D;
                @Config.Comment("Cost increment when switching trees")
                public double costIncrement = 0.9D;
                @Config.Comment("Percentage of all xp spent that is returned on reset")
                public double retryXPReturn = 0.8D;
                @Config.Comment("Percentage of xp stored")
                public double xpStoreTariff = 1D;
                @Config.Comment("Percentage of xp taken")
                public double xpTakeTariff = 0.8D;
            }

            public static class Levels {
                public String[] function = {
                        "(0+){0}",
                        "(1+){1}",
                        "(2+){x/2}",
                        "(3+){x/4}",
                        "(4+){x/6}",
                        "(5+){x/8}",
                        "(6+){x/10}",
                        "(7+){x/12}",
                        "(8+){x/14}",
                        "(9+){x/(2 ^ x) * 0.1}"
                };
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

        public static class WorldDrops {

            @Config.Comment("Chance of ender token drops?")
            @Config.RangeDouble(min = 0D, max = 1D)
            public double enderTokenDropRate = 0.07D;

            @Config.Comment("Toggle ender token drops")
            public boolean enderTokenDropToggle = true;
        }
    }
}
