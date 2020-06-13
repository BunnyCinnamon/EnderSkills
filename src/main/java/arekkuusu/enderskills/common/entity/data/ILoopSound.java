package arekkuusu.enderskills.common.entity.data;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILoopSound {
    @SideOnly(Side.CLIENT)
    void makeSound(Entity source);
}
