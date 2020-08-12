package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.ability.defense.earth.*;
import arekkuusu.enderskills.common.skill.ability.defense.electric.ElectricPulse;
import arekkuusu.enderskills.common.skill.ability.defense.electric.ShockingAura;
import arekkuusu.enderskills.common.skill.ability.defense.light.*;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.*;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.*;
import arekkuusu.enderskills.common.skill.ability.offence.blood.*;
import arekkuusu.enderskills.common.skill.ability.offence.ender.*;
import arekkuusu.enderskills.common.skill.ability.offence.fire.*;
import arekkuusu.enderskills.common.skill.ability.offence.wind.*;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(LibMod.MOD_ID)
public final class ModAbilities {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T extends Skill> T empty() {
        return null;
    }

    //Defense-Light
    public static final Skill CHARM = empty();
    public static final Skill HEAL_AURA = empty();
    public static final Skill POWER_BOOST = empty();
    public static final Skill HEAL_OTHER = empty();
    public static final Skill HEAL_SELF = empty();
    public static final Skill NEARBY_INVINCIBILITY = empty();
    //Defense-Earth
    public static final Skill TAUNT = empty();
    public static final Skill WALL = empty();
    public static final Skill DOME = empty();
    public static final Skill THORNY = empty();
    public static final Skill SHOCKWAVE = empty();
    public static final BaseAbility ANIMATED_STONE_GOLEM = empty();
    //Defense-Electric
    public static final Skill SHOCKING_AURA = empty();
    public static final Skill ELECTRIC_PULSE = empty();
    //Mobility-Wind
    public static final Dash DASH = empty();
    public static final Skill EXTRA_JUMP = empty();
    public static final Skill FOG = empty();
    public static final Skill SMASH = empty();
    public static final Skill HASTEN = empty();
    public static final Skill SPEED_BOOST = empty();
    //Mobility-Void
    public static final Warp WARP = empty();
    public static final Skill INVISIBILITY = empty();
    public static final Skill HOVER = empty();
    public static final Skill UNSTABLE_PORTAL = empty();
    public static final Skill PORTAL = empty();
    public static final Skill TELEPORT = empty();
    //Offense-Void
    public static final Skill SHADOW = empty();
    public static final Skill GLOOM = empty();
    public static final Skill SHADOW_JAB = empty();
    public static final Skill GAS_CLOUD = empty();
    public static final Skill GRASP = empty();
    public static final Skill BLACK_HOLE = empty();
    //Offense-Blood
    public static final Bleed BLEED = empty();
    public static final BloodPool BLOOD_POOL = empty();
    public static final Contaminate CONTAMINATE = empty();
    public static final Skill LIFE_STEAL = empty();
    public static final Skill SYPHON = empty();
    public static final Skill SACRIFICE = empty();
    //Offense-Wind
    public static final Skill SLASH = empty();
    public static final Skill PUSH = empty();
    public static final Skill PULL = empty();
    public static final Skill CRUSH = empty();
    public static final Skill UPDRAFT = empty();
    public static final Skill SUFFOCATE = empty();
    //Offense-Fire
    public static final Skill FIRE_SPIRIT = empty();
    public static final Skill FLAMING_BREATH = empty();
    public static final Skill FLAMING_RAIN = empty();
    public static final Skill FOCUS_FLAME = empty();
    public static final Skill FIREBALL = empty();
    public static final Skill EXPLODE = empty();

    public static void register(IForgeRegistry<Skill> registry) {
        //Defense-Light
        registry.register(new Charm());
        registry.register(new HealAura());
        registry.register(new PowerBoost());
        registry.register(new HealOther());
        registry.register(new HealSelf());
        registry.register(new NearbyInvincibility());
        //Defense-Earth
        registry.register(new Taunt());
        registry.register(new Wall());
        registry.register(new Dome());
        registry.register(new Thorny());
        registry.register(new Shockwave());
        registry.register(new AnimatedStoneGolem());
        //Defense-Electric
        //registry.register(new ShockingAura());
        //registry.register(new ElectricPulse());
        //Mobility-Wind
        registry.register(new Dash());
        registry.register(new ExtraJump());
        registry.register(new Fog());
        registry.register(new Smash());
        registry.register(new Hasten());
        registry.register(new SpeedBoost());
        //Mobility-Void
        registry.register(new Warp());
        registry.register(new Invisibility());
        registry.register(new Hover());
        registry.register(new UnstablePortal());
        registry.register(new Portal());
        registry.register(new Teleport());
        //Offense-Void
        registry.register(new Shadow());
        registry.register(new Gloom());
        registry.register(new ShadowJab());
        registry.register(new GasCloud());
        registry.register(new Grasp());
        registry.register(new BlackHole());
        //Offense-Blood
        registry.register(new Bleed());
        registry.register(new BloodPool());
        registry.register(new Contaminate());
        registry.register(new LifeSteal());
        registry.register(new Syphon());
        registry.register(new Sacrifice());
        //Offense-Wind
        registry.register(new Slash());
        registry.register(new Push());
        registry.register(new Pull());
        registry.register(new Crush());
        registry.register(new Updraft());
        registry.register(new Suffocate());
        //Offense-Fire
        registry.register(new FireSpirit());
        registry.register(new FlamingBreath());
        registry.register(new FlamingRain());
        registry.register(new FocusFlame());
        registry.register(new Fireball());
        registry.register(new Explode());
    }

    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"}) //Shut up
    public static Skill setRegistry(Skill skill, String id) {
        skill.setRegistryName(LibMod.MOD_ID, id);
        return skill;
    }
}
