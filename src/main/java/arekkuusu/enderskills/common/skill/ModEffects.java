package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.effect.*;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(LibMod.MOD_ID)
public class ModEffects {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T extends Skill> T empty() {
        return null;
    }

    public static final Overheal OVERHEAL = empty();
    public static final BaseEffect ELECTRIFIED = empty();
    public static final BaseEffect OVERCHARGE = empty();
    public static final Stunned STUNNED = empty();
    public static final BaseEffect INVULNERABLE = empty();
    public static final BaseEffect BLEEDING = empty();
    public static final BaseEffect BURNING = empty();
    public static final Slowed SLOWED = empty();
    public static final BaseEffect VOIDED = empty();
    public static final BaseEffect ROOTED = empty();
    public static final BaseEffect BLINDED = empty();

    public static void register(IForgeRegistry<Skill> registry) {
        registry.register(new Overheal());
        //registry.register(new Electrified());
        //registry.register(new Overcharge());
        registry.register(new Stunned());
        registry.register(new Invulnerable());
        registry.register(new Bleeding());
        registry.register(new Burning());
        registry.register(new Slowed());
        registry.register(new Voided());
        registry.register(new Rooted());
        registry.register(new Blinded());
    }
}
