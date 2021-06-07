package arekkuusu.enderskills.common;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.IConfigSync;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;

@Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/global")
public final class CommonConfig {

    @Config.Ignore
    public static CommonConfig.Values CONFIG_SYNC = new CommonConfig.Values();
    public static CommonConfig.Values CONFIG = new CommonConfig.Values();

    public static Values getValues() {
        return CONFIG;
    }

    @Deprecated
    public static void initSyncConfig() {
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        for (Map.Entry<ResourceLocation, Skill> entry : registry.getEntries()) {
            if (entry.getValue() instanceof IConfigSync) {
                ((IConfigSync) entry.getValue()).initSyncConfig();
            }
        }
    }

    @Deprecated
    public static void writeSyncConfig(NBTTagCompound compound) {
        compound.setBoolean("advancement.oneTreePerClass", CommonConfig.getValues().advancement.oneTreePerClass);
        compound.setDouble("advancement.xp.globalCostMultiplier", CommonConfig.getValues().advancement.xp.globalCostMultiplier);
        compound.setDouble("advancement.xp.costIncrement", CommonConfig.getValues().advancement.xp.costIncrement);
        compound.setDouble("advancement.xp.retryXPReturn", CommonConfig.getValues().advancement.xp.retryXPReturn);
        compound.setDouble("advancement.xp.xpStoreTariff", CommonConfig.getValues().advancement.xp.xpStoreTariff);
        compound.setDouble("advancement.xp.xpTakeTariff", CommonConfig.getValues().advancement.xp.xpTakeTariff);
        compound.setInteger("advancement.maxResetUnlocks", CommonConfig.getValues().advancement.maxResetUnlocks);
        NBTHelper.setArray(compound, "advancement.levels.function", CommonConfig.getValues().advancement.levels.function);
        compound.setInteger("advancement.levels.defaultLevel", CommonConfig.getValues().advancement.levels.defaultLevel);
        compound.setBoolean("destroyBlocks", CommonConfig.getValues().skill.destroyBlocks);
        compound.setBoolean("preventAbilityDoTKnockback", CommonConfig.getValues().skill.preventAbilityDoTKnockback);
        compound.setBoolean("defaultHumanTeam", CommonConfig.getValues().skill.defaultHumanTeam);
        compound.setBoolean("defaultAnimalTeam", CommonConfig.getValues().skill.defaultAnimalTeam);
        compound.setDouble("globalPositiveEffect", CommonConfig.getValues().skill.globalPositiveEffect);
        compound.setDouble("globalNegativeEffect", CommonConfig.getValues().skill.globalNegativeEffect);
        EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.invalidateAll();
        EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.cleanUp();
        EnderSkillsAPI.EXPRESSION_CACHE.invalidateAll();
        EnderSkillsAPI.EXPRESSION_CACHE.cleanUp();
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public static void readSyncConfig(NBTTagCompound compound) {
        CommonConfig.getValues().advancement.oneTreePerClass = compound.getBoolean("advancement.oneTreePerClass");
        CommonConfig.getValues().advancement.xp.globalCostMultiplier = compound.getDouble("advancement.xp.globalCostMultiplier");
        CommonConfig.getValues().advancement.xp.costIncrement = compound.getDouble("advancement.xp.costIncrement");
        CommonConfig.getValues().advancement.xp.retryXPReturn = compound.getDouble("advancement.xp.retryXPReturn");
        CommonConfig.getValues().advancement.xp.xpStoreTariff = compound.getDouble("advancement.xp.xpStoreTariff");
        CommonConfig.getValues().advancement.xp.xpTakeTariff = compound.getDouble("advancement.xp.xpTakeTariff");
        CommonConfig.getValues().advancement.maxResetUnlocks = compound.getInteger("advancement.maxResetUnlocks");
        CommonConfig.getValues().advancement.levels.function = NBTHelper.getArray(compound, "advancement.levels.function");
        CommonConfig.getValues().advancement.levels.defaultLevel = compound.getInteger("advancement.levels.defaultLevel");
        CommonConfig.getValues().skill.destroyBlocks = compound.getBoolean("destroyBlocks");
        CommonConfig.getValues().skill.preventAbilityDoTKnockback = compound.getBoolean("preventAbilityDoTKnockback");
        CommonConfig.getValues().skill.defaultHumanTeam = compound.getBoolean("defaultHumanTeam");
        CommonConfig.getValues().skill.defaultAnimalTeam = compound.getBoolean("defaultAnimalTeam");
        CommonConfig.getValues().skill.globalPositiveEffect = compound.getDouble("globalPositiveEffect");
        CommonConfig.getValues().skill.globalNegativeEffect = compound.getDouble("globalNegativeEffect");
        EnderSkillsAPI.defaultHumanTeam = CommonConfig.getValues().skill.defaultHumanTeam;
        EnderSkillsAPI.defaultAnimalTeam = CommonConfig.getValues().skill.defaultAnimalTeam;
        EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.invalidateAll();
        EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.cleanUp();
        EnderSkillsAPI.EXPRESSION_CACHE.invalidateAll();
        EnderSkillsAPI.EXPRESSION_CACHE.cleanUp();
    }

    public static class Values {

        public final SkillGlobalConfig skill = new SkillGlobalConfig();
        public final SkillAdvancementConfig advancement = new SkillAdvancementConfig();
        public final WorldGen worldGen = new WorldGen();

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

            @Config.Comment("Modifies negative effects of all abilities (used for balancing damage)")
            public double globalPositiveEffect = 1D;

            @Config.Comment("Modifies positive effects of all abilities (used for balancing heals)")
            public double globalNegativeEffect = 1D;
        }

        public static class SkillAdvancementConfig {

            public final Experience xp = new Experience();
            public final Levels levels = new Levels();

            public boolean oneTreePerClass = true;

            public int maxResetUnlocks = 2;

            public static class Experience {
                @Config.Comment("Cost increment of all advancements")
                public double globalCostMultiplier = 1D;
                @Config.Comment("Cost increment when switching trees")
                public double costIncrement = 0.5D;
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
    }
}
