package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.common.entity.placeable.EntityFinalFlash;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
public class FinalFlashSound extends MovingSound {

    public final WeakReference<EntityFinalFlash> reference;

    public FinalFlashSound(EntityFinalFlash entity) {
        super(ModSounds.FINAL_FLASH_RELEASE, SoundCategory.PLAYERS);
        this.reference = new WeakReference<>(entity);
        this.xPosF = (float) entity.posX;
        this.yPosF = (float) entity.posY;
        this.zPosF = (float) entity.posZ;
        this.repeat = true;
        this.volume = 8;
    }

    @Override
    public void update() {
        EntityFinalFlash entity = reference.get();
        donePlaying = entity == null || entity.isDead;
    }

    @Override
    public float getVolume() {
        EntityFinalFlash entity = reference.get();
        return super.getVolume() * (entity == null ? 0F : 1F - ((float) entity.tick / (float) (entity.getLifeTime() + 20F)));
    }
}
