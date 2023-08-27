package arekkuusu.enderskills.api.helper;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class SoundHelper {

    public static void playSound(World world, BlockPos pos, SoundEvent soundEvent) {
        if (world instanceof WorldServer) {
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), soundEvent, SoundCategory.PLAYERS, 5.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
}
