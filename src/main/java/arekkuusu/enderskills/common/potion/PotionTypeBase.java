package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;

public class PotionTypeBase extends PotionType {

    public PotionTypeBase(String name, PotionEffect... effects) {
        super(LibMod.MOD_ID + ".potion_type_" + name, effects);
        setRegistryName(LibMod.MOD_ID, "potion_type_" + name);
    }
}
