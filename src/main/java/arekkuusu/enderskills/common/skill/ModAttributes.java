package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.attribute.deffense.*;
import arekkuusu.enderskills.common.skill.attribute.mobility.*;
import arekkuusu.enderskills.common.skill.attribute.offense.*;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(LibMod.MOD_ID)
public final class ModAttributes {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T extends Skill> T empty() {
        return null;
    }

    public static final Skill EXPLOSION_RESISTANCE = empty();
    public static final DamageResistance DAMAGE_RESISTANCE = empty();
    public static final Skill KNOCKBACK_RESISTANCE = empty();
    public static final Skill MAGIC_RESISTANCE = empty();
    public static final Skill HEART_BOOST = empty();
    public static final Skill FIRE_RESISTANCE = empty();
    public static final Skill JUMP_HEIGHT = empty();
    public static final Skill SPEED = empty();
    public static final Skill FALL_RESISTANCE = empty();
    public static final Endurance ENDURANCE = empty();
    public static final StealthDamage STEALTH_DAMAGE = empty();
    public static final Skill SWIM_SPEED = empty();
    public static final MeleeDamage DAMAGE = empty();
    public static final Skill ATTACK_SPEED = empty();
    public static final Skill KNOCKBACK = empty();
    public static final AbilityPower ABILITY_POWER = empty();
    public static final AbilityPower ABILITY_DURATION = empty();
    public static final AbilityPower ABILITY_RANGE = empty();
    public static final CriticalChance CRITICAL_CHANCE = empty();
    public static final ArmorPenetration ARMOR_PENETRATION = empty();

    public static void register(IForgeRegistry<Skill> registry) {
        registry.register(new ExplosionResistance());
        registry.register(new DamageResistance());
        registry.register(new KnockbackResistance());
        registry.register(new MagicResistance());
        registry.register(new HeartBoost());
        registry.register(new FireResistance());
        registry.register(new JumpHeight());
        registry.register(new Speed());
        registry.register(new FallResistance());
        registry.register(new Endurance());
        registry.register(new StealthDamage());
        registry.register(new SwimSpeed());
        registry.register(new MeleeDamage());
        registry.register(new AttackSpeed());
        registry.register(new Knockback());
        registry.register(new AbilityPower());
        registry.register(new CriticalChance());
        registry.register(new ArmorPenetration());
        registry.register(new AbilityDuration());
        registry.register(new AbilityRange());
    }

    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"}) //Shut up
    public static Skill setRegistry(Skill skill, String id) {
        skill.setRegistryName(LibMod.MOD_ID, id);
        return skill;
    }
}
