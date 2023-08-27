package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.configuration.network.*;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.defense.earth.*;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
import arekkuusu.enderskills.common.skill.effect.Glowing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Map;

@ObjectHolder(LibMod.MOD_ID)
public class ModConfigurations {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T extends ConfigSynchronizer> T empty() {
        return null;
    }

    public static final ConfigSynchronizer ANIMATED_STONE_GOLEM = empty();
    public static final ConfigSynchronizer GLOWING = empty();

    public static void register(IForgeRegistry<ConfigSynchronizer> registry) {
        // Global configuration
        registry.register(new FieldConfigSynchronizer(CommonConfig.Values.class, "skill", CommonConfig.getSyncValues(), CommonConfig.getConfig(), "skill"));
        registry.register(new FieldConfigSynchronizer(CommonConfig.Values.class, "advancement", CommonConfig.getSyncValues(), CommonConfig.getConfig(), "advancement"));
        registry.register(new StaticFieldConfigSynchronizer(Endurance.Configuration.class, "LOCAL_VALUES", "CONFIG", LibNames.ENDURANCE + "_map"));
        registry.register(new ConfigSynchronizerListener("listener") {
            @Override
            public void update() {
                EnderSkillsAPI.defaultHumanTeam = CommonConfig.getSyncValues().skill.defaultHumanTeam;
                EnderSkillsAPI.defaultAnimalTeam = CommonConfig.getSyncValues().skill.defaultAnimalTeam;
                EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.invalidateAll();
                EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.cleanUp();
                EnderSkillsAPI.EXPRESSION_CACHE.invalidateAll();
                EnderSkillsAPI.EXPRESSION_CACHE.cleanUp();
            }
        });
        // Others
        registry.register(new DSLConfigSynchronizer(AnimatedStoneGolem.Configuration.CONFIG, LibNames.ANIMATED_STONE_GOLEM));
        registry.register(new DSLConfigSynchronizer(Dome.Configuration.CONFIG, LibNames.DOME));
        registry.register(new DSLConfigSynchronizer(Shockwave.Configuration.CONFIG, LibNames.SHOCKWAVE));
        registry.register(new DSLConfigSynchronizer(Taunt.Configuration.CONFIG, LibNames.TAUNT));
        registry.register(new DSLConfigSynchronizer(Thorny.Configuration.CONFIG, LibNames.THORNY));
        registry.register(new DSLConfigSynchronizer(Wall.Configuration.CONFIG, LibNames.WALL));
        //
        registry.register(new DSLConfigSynchronizer(Glowing.Configuration.CONFIG, LibNames.GLOWING));
        registry.register(new DSLConfigSynchronizer(Endurance.Configuration.CONFIG, LibNames.ENDURANCE));
    }

    public static ConfigSynchronizer setRegistry(ConfigSynchronizer configSynchronizer, String id) {
        configSynchronizer.setRegistryName(LibMod.MOD_ID, id);
        return configSynchronizer;
    }

    public static void init() {
        IForgeRegistry<ConfigSynchronizer> registry = GameRegistry.findRegistry(ConfigSynchronizer.class);
        for (Map.Entry<ResourceLocation, ConfigSynchronizer> entry : registry.getEntries()) {
            entry.getValue().initSyncConfig();
        }
    }
}
