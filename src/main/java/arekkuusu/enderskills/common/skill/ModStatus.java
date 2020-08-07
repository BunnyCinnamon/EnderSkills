package arekkuusu.enderskills.common.skill;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.status.OverHeal;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(LibMod.MOD_ID)
public class ModStatus {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T extends Skill> T empty() {
        return null;
    }

    public static final Skill OVER_HEAL = empty();

    public static void register(IForgeRegistry<Skill> registry) {
        registry.register(new OverHeal());
    }
}
