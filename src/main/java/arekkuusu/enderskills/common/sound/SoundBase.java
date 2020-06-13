package arekkuusu.enderskills.common.sound;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class SoundBase extends SoundEvent {

    public SoundBase(String name) {
        super(new ResourceLocation(LibMod.MOD_ID, name));
        setRegistryName(LibMod.MOD_ID, name);
    }
}
