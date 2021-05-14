package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(LibMod.MOD_ID)
public final class ModPotionTypes {

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static PotionType empty() {
        return null;
    }

    public static final PotionType POTION_TYPE_ENDER_EFFECT = empty();
    public static final PotionType POTION_TYPE_ABILITY_POWER_EFFECT_NORMAL = empty();
    public static final PotionType POTION_TYPE_ABILITY_POWER_EFFECT_LONG = empty();
    public static final PotionType POTION_TYPE_ABILITY_POWER_EFFECT_STRONG = empty();
    public static final PotionType POTION_TYPE_COOLDOWN_EFFECT_NORMAL = empty();
    public static final PotionType POTION_TYPE_COOLDOWN_EFFECT_LONG = empty();
    public static final PotionType POTION_TYPE_COOLDOWN_EFFECT_STRONG = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_EFFECT_NORMAL = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_EFFECT_LONG = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_EFFECT_STRONG = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_REGEN_EFFECT_NORMAL = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_REGEN_EFFECT_LONG = empty();
    public static final PotionType POTION_TYPE_ENDURANCE_REGEN_EFFECT_STRONG = empty();

    public static void register(IForgeRegistry<PotionType> registry) {
        registry.register(new PotionTypeBase(LibNames.ENDER_EFFECT));
        registry.register(new PotionTypeBase(LibNames.ABILITY_POWER_EFFECT + "_normal", new PotionEffect(ModPotions.POTION_ABILITY_POWER_EFFECT, 20 * (60 * 1)/* five minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ABILITY_POWER_EFFECT + "_long", new PotionEffect(ModPotions.POTION_ABILITY_POWER_EFFECT, 20 * (60 * 10)/* ten minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ABILITY_POWER_EFFECT + "_strong", new PotionEffect(ModPotions.POTION_ABILITY_POWER_EFFECT, 20 * (60 * 5)/* one minute */, 1)));
        registry.register(new PotionTypeBase(LibNames.COOLDOWN_EFFECT + "_normal", new PotionEffect(ModPotions.POTION_COOLDOWN_EFFECT, 20 * (60 * 1)/* five minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.COOLDOWN_EFFECT + "_long", new PotionEffect(ModPotions.POTION_COOLDOWN_EFFECT, 20 * (60 * 10)/* ten minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.COOLDOWN_EFFECT + "_strong", new PotionEffect(ModPotions.POTION_COOLDOWN_EFFECT, 20 * (60 * 5)/* one minute */, 1)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_EFFECT + "_normal", new PotionEffect(ModPotions.POTION_ENDURANCE_EFFECT, 20 * (60 * 1)/* five minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_EFFECT + "_long", new PotionEffect(ModPotions.POTION_ENDURANCE_EFFECT, 20 * (60 * 10)/* ten minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_EFFECT + "_strong", new PotionEffect(ModPotions.POTION_ENDURANCE_EFFECT, 20 * (60 * 5)/* one minute */, 1)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_REGEN_EFFECT + "_normal", new PotionEffect(ModPotions.ENDURANCE_REGEN_EFFECT, 20 * (60 * 1)/* five minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_REGEN_EFFECT + "_long", new PotionEffect(ModPotions.ENDURANCE_REGEN_EFFECT, 20 * (60 * 10)/* ten minutes */, 0)));
        registry.register(new PotionTypeBase(LibNames.ENDURANCE_REGEN_EFFECT + "_strong", new PotionEffect(ModPotions.ENDURANCE_REGEN_EFFECT, 20 * (60 * 5)/* one minute */, 1)));
    }

    public static void init() {
        PotionHelper.addMix(PotionTypes.WATER, Items.ENDER_EYE, ModPotionTypes.POTION_TYPE_ENDER_EFFECT);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDER_EFFECT, Items.GOLDEN_APPLE, ModPotionTypes.POTION_TYPE_ABILITY_POWER_EFFECT_NORMAL);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ABILITY_POWER_EFFECT_NORMAL, Item.getItemFromBlock(Blocks.REDSTONE_BLOCK), ModPotionTypes.POTION_TYPE_ABILITY_POWER_EFFECT_LONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ABILITY_POWER_EFFECT_NORMAL, ModItems.TOKEN, ModPotionTypes.POTION_TYPE_ABILITY_POWER_EFFECT_STRONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDER_EFFECT, Items.GOLDEN_CARROT, ModPotionTypes.POTION_TYPE_COOLDOWN_EFFECT_NORMAL);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_COOLDOWN_EFFECT_NORMAL, Item.getItemFromBlock(Blocks.REDSTONE_BLOCK), ModPotionTypes.POTION_TYPE_COOLDOWN_EFFECT_LONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_COOLDOWN_EFFECT_NORMAL, ModItems.TOKEN, ModPotionTypes.POTION_TYPE_COOLDOWN_EFFECT_STRONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDER_EFFECT, Items.SPECKLED_MELON, ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_NORMAL);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_NORMAL, Item.getItemFromBlock(Blocks.REDSTONE_BLOCK), ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_LONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_NORMAL, ModItems.TOKEN, ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_STRONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDURANCE_EFFECT_NORMAL, Items.ENDER_EYE, ModPotionTypes.POTION_TYPE_ENDURANCE_REGEN_EFFECT_NORMAL);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDURANCE_REGEN_EFFECT_NORMAL, Items.GLOWSTONE_DUST, ModPotionTypes.POTION_TYPE_ENDURANCE_REGEN_EFFECT_LONG);
        PotionHelper.addMix(ModPotionTypes.POTION_TYPE_ENDURANCE_REGEN_EFFECT_NORMAL, Items.REDSTONE, ModPotionTypes.POTION_TYPE_ENDURANCE_REGEN_EFFECT_STRONG);
    }
}
