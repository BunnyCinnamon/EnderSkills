package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(LibMod.MOD_ID)
public final class ModPotions {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static Potion empty() {
        return null;
    }

    public static Potion POTION_ABILITY_POWER_EFFECT = empty();
    public static Potion POTION_COOLDOWN_EFFECT = empty();
    public static Potion POTION_ENDURANCE_EFFECT = empty();

    public static void register(IForgeRegistry<Potion> registry) {
        registry.register(POTION_ABILITY_POWER_EFFECT = new PotionAbilityPower());
        registry.register(POTION_COOLDOWN_EFFECT = new PotionCooldown());
        registry.register(POTION_ENDURANCE_EFFECT = new PotionEndurance());
    }
}
