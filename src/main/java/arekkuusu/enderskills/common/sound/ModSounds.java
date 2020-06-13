package arekkuusu.enderskills.common.sound;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(LibMod.MOD_ID)
public final class ModSounds {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static SoundEvent empty() {
        return null;
    }

    public static final SoundEvent PAGE_TURN = empty();
    public static final SoundEvent SPARK = empty();
    public static final SoundEvent PORTAL_PLACE = empty();
    public static final SoundEvent PORTAL_ACTIVE = empty();
    public static final SoundEvent PORTAL_CLOSED_ACTIVE = empty();
    public static final SoundEvent PORTAL_OPEN = empty();
    public static final SoundEvent TELEPORT = empty();
    public static final SoundEvent UNSTABLE_PORTAL = empty();
    public static final SoundEvent WARP = empty();
    public static final SoundEvent INVISIBILITY = empty();
    public static final SoundEvent HOVER = empty();
    public static final SoundEvent SMASH_HIT = empty();
    public static final SoundEvent SMASH_START = empty();
    public static final SoundEvent SPEED = empty();
    public static final SoundEvent DASH = empty();
    public static final SoundEvent FOG = empty();
    public static final SoundEvent HASTEN = empty();
    public static final SoundEvent JUMP = empty();
    public static final SoundEvent PUSH = empty();
    public static final SoundEvent PULL = empty();
    public static final SoundEvent SLASH = empty();
    public static final SoundEvent UPDRAFT = empty();
    public static final SoundEvent WIND_ON_HIT = empty();
    public static final SoundEvent SUFFOCATE = empty();
    public static final SoundEvent SUFFOCATE_ACTIVE = empty();
    public static final SoundEvent CRUSH = empty();
    public static final SoundEvent TAUNT = empty();
    public static final SoundEvent THORNS = empty();
    public static final SoundEvent WALL_UP = empty();
    public static final SoundEvent WALL_DOWN = empty();
    public static final SoundEvent ANIMATED_STONE = empty();
    public static final SoundEvent DOME_DOWN = empty();
    public static final SoundEvent DOME_UP = empty();
    public static final SoundEvent EARTH_HIT = empty();
    public static final SoundEvent SHOCKWAVE = empty();
    public static final SoundEvent CONTAMINATE = empty();
    public static final SoundEvent BLOODPOOL_ACTIVE = empty();
    public static final SoundEvent BLOODPOOL = empty();
    public static final SoundEvent LIFE_STEAL = empty();
    public static final SoundEvent LIFE_STEAL_ATTACK = empty();
    public static final SoundEvent SACRIFICE = empty();
    public static final SoundEvent SACRIFICE_ACTIVE = empty();
    public static final SoundEvent SYPHON = empty();
    public static final SoundEvent BLEED = empty();
    public static final SoundEvent BLEED_ACTIVE = empty();
    public static final SoundEvent GAS_CLOUD = empty();
    public static final SoundEvent GAS_CLOUD_EXPLODE = empty();
    public static final SoundEvent GLOOM = empty();
    public static final SoundEvent GRASP = empty();
    public static final SoundEvent GRASP_ACTIVE = empty();
    public static final SoundEvent VOID_HIT = empty();
    public static final SoundEvent SHADOW = empty();
    public static final SoundEvent SHADOW_ATTACK = empty();
    public static final SoundEvent SHADOWJAB = empty();
    public static final SoundEvent BLACKHOLE = empty();
    public static final SoundEvent BLACKHOLE_ACTIVE = empty();
    public static final SoundEvent FIRE_HIT = empty();
    public static final SoundEvent FIRE_SPIRIT = empty();
    public static final SoundEvent FIRE_SPIRIT_ACTIVE = empty();
    public static final SoundEvent FIRE_SPIRIT_ACTIVE2 = empty();
    public static final SoundEvent FIREBALL = empty();
    public static final SoundEvent FIREBALL_EXPLODE = empty();
    public static final SoundEvent FLAMING_BREATH = empty();
    public static final SoundEvent FLAMING_RAIN = empty();
    public static final SoundEvent FLAMING_RAIN_ACTIVE = empty();
    public static final SoundEvent FOCUS_FLAME = empty();
    public static final SoundEvent EXPLODE = empty();

    public static void register(IForgeRegistry<SoundEvent> registry) {
        registry.register(new SoundBase("page_turn"));
        registry.register(new SoundBase("spark"));
        registry.register(new SoundBase("portal_place"));
        registry.register(new SoundBase("portal_active"));
        registry.register(new SoundBase("portal_closed_active"));
        registry.register(new SoundBase("portal_open"));
        registry.register(new SoundBase("teleport"));
        registry.register(new SoundBase("unstable_portal"));
        registry.register(new SoundBase("warp"));
        registry.register(new SoundBase("invisibility"));
        registry.register(new SoundBase("hover"));
        registry.register(new SoundBase("smash_hit"));
        registry.register(new SoundBase("smash_start"));
        registry.register(new SoundBase("dash"));
        registry.register(new SoundBase("speed"));
        registry.register(new SoundBase("fog"));
        registry.register(new SoundBase("hasten"));
        registry.register(new SoundBase("jump"));
        registry.register(new SoundBase("pull"));
        registry.register(new SoundBase("push"));
        registry.register(new SoundBase("slash"));
        registry.register(new SoundBase("updraft"));
        registry.register(new SoundBase("wind_on_hit"));
        registry.register(new SoundBase("suffocate"));
        registry.register(new SoundBase("suffocate_active"));
        registry.register(new SoundBase("crush"));
        registry.register(new SoundBase("taunt"));
        registry.register(new SoundBase("thorns"));
        registry.register(new SoundBase("wall_down"));
        registry.register(new SoundBase("wall_up"));
        registry.register(new SoundBase("animated_stone"));
        registry.register(new SoundBase("dome_down"));
        registry.register(new SoundBase("dome_up"));
        registry.register(new SoundBase("earth_hit"));
        registry.register(new SoundBase("shockwave"));
        registry.register(new SoundBase("contaminate"));
        registry.register(new SoundBase("bloodpool_active"));
        registry.register(new SoundBase("bloodpool"));
        registry.register(new SoundBase("life_steal"));
        registry.register(new SoundBase("life_steal_attack"));
        registry.register(new SoundBase("sacrifice"));
        registry.register(new SoundBase("sacrifice_active"));
        registry.register(new SoundBase("syphon"));
        registry.register(new SoundBase("bleed"));
        registry.register(new SoundBase("bleed_active"));
        registry.register(new SoundBase("gas_cloud_explode"));
        registry.register(new SoundBase("gloom"));
        registry.register(new SoundBase("grasp"));
        registry.register(new SoundBase("grasp_active"));
        registry.register(new SoundBase("void_hit"));
        registry.register(new SoundBase("shadow"));
        registry.register(new SoundBase("shadow_attack"));
        registry.register(new SoundBase("shadowjab"));
        registry.register(new SoundBase("blackhole"));
        registry.register(new SoundBase("blackhole_active"));
        registry.register(new SoundBase("gas_cloud"));
        registry.register(new SoundBase("fire_hit"));
        registry.register(new SoundBase("fire_spirit"));
        registry.register(new SoundBase("fire_spirit_active"));
        registry.register(new SoundBase("fire_spirit_active2"));
        registry.register(new SoundBase("fireball"));
        registry.register(new SoundBase("fireball_explode"));
        registry.register(new SoundBase("flaming_rain"));
        registry.register(new SoundBase("focus_flame"));
        registry.register(new SoundBase("flaming_rain_active"));
        registry.register(new SoundBase("explode"));
        registry.register(new SoundBase("flaming_breath"));
    }
}
