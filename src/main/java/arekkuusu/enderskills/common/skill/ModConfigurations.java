package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.configuration.network.*;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.defense.earth.*;
import arekkuusu.enderskills.common.skill.ability.defense.electric.*;
import arekkuusu.enderskills.common.skill.ability.defense.fire.*;
import arekkuusu.enderskills.common.skill.ability.defense.light.*;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.*;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.*;
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
        registry.register(new StaticFieldConfigSynchronizer(Endurance.Configuration.class, "LOCAL_VALUES", "LOCAL", LibNames.ENDURANCE + "_map"));
        registry.register(new StaticFieldConfigSynchronizer(Overheat.Configuration.class, "LOCAL_VALUES", "LOCAL", LibNames.OVERHEAT + "_enum"));
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
        registry.register(new DSLConfigSynchronizer(ElectricPulse.Configuration.CONFIG, LibNames.ELECTRIC_PULSE));
        registry.register(new DSLConfigSynchronizer(Energize.Configuration.CONFIG, LibNames.ENERGIZE));
        registry.register(new DSLConfigSynchronizer(MagneticPull.Configuration.CONFIG, LibNames.MAGNETIC_PULL));
        registry.register(new DSLConfigSynchronizer(PowerDrain.Configuration.CONFIG, LibNames.POWER_DRAIN));
        registry.register(new DSLConfigSynchronizer(ShockingAura.Configuration.CONFIG, LibNames.SHOCKING_AURA));
        registry.register(new DSLConfigSynchronizer(VoltaicSentinel.Configuration.CONFIG, LibNames.VOLTAIC_SENTINEL));
        registry.register(new DSLConfigSynchronizer(BlazingAura.Configuration.CONFIG, LibNames.BLAZING_AURA));
        registry.register(new DSLConfigSynchronizer(Flares.Configuration.CONFIG, LibNames.FLARES));
        registry.register(new DSLConfigSynchronizer(HomeStar.Configuration.CONFIG, LibNames.HOME_STAR));
        registry.register(new DSLConfigSynchronizer(Overheat.Configuration.CONFIG, LibNames.OVERHEAT));
        registry.register(new DSLConfigSynchronizer(RingOfFire.Configuration.CONFIG, LibNames.RING_OF_FIRE));
        registry.register(new DSLConfigSynchronizer(WarmHeart.Configuration.CONFIG, LibNames.WARM_HEART));
        registry.register(new DSLConfigSynchronizer(Charm.Configuration.CONFIG, LibNames.CHARM));
        registry.register(new DSLConfigSynchronizer(HealAura.Configuration.CONFIG, LibNames.HEAL_AURA));
        registry.register(new DSLConfigSynchronizer(HealOther.Configuration.CONFIG, LibNames.HEAL_OTHER));
        registry.register(new DSLConfigSynchronizer(HealSelf.Configuration.CONFIG, LibNames.HEAL_SELF));
        registry.register(new DSLConfigSynchronizer(NearbyInvincibility.Configuration.CONFIG, LibNames.NEARBY_INVINCIBILITY));
        registry.register(new DSLConfigSynchronizer(PowerBoost.Configuration.CONFIG, LibNames.POWER_BOOST));
        registry.register(new DSLConfigSynchronizer(Hover.Configuration.CONFIG, LibNames.HOVER));
        registry.register(new DSLConfigSynchronizer(Invisibility.Configuration.CONFIG, LibNames.INVISIBILITY));
        registry.register(new DSLConfigSynchronizer(Portal.Configuration.CONFIG, LibNames.PORTAL));
        registry.register(new DSLConfigSynchronizer(Teleport.Configuration.CONFIG, LibNames.TELEPORT));
        registry.register(new DSLConfigSynchronizer(UnstablePortal.Configuration.CONFIG, LibNames.UNSTABLE_PORTAL));
        registry.register(new DSLConfigSynchronizer(Warp.Configuration.CONFIG, LibNames.WARP));
        registry.register(new DSLConfigSynchronizer(Dash.Configuration.CONFIG, LibNames.DASH));
        registry.register(new DSLConfigSynchronizer(ExtraJump.Configuration.CONFIG, LibNames.EXTRA_JUMP));
        registry.register(new DSLConfigSynchronizer(Fog.Configuration.CONFIG, LibNames.FOG));
        registry.register(new DSLConfigSynchronizer(Hasten.Configuration.CONFIG, LibNames.HASTEN));
        registry.register(new DSLConfigSynchronizer(Smash.Configuration.CONFIG, LibNames.SMASH));
        registry.register(new DSLConfigSynchronizer(SpeedBoost.Configuration.CONFIG, LibNames.SPEED_BOOST));
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
