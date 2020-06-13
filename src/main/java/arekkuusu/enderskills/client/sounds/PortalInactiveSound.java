package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.common.entity.EntityPortal;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
public class PortalInactiveSound extends MovingSound {

    public final WeakReference<EntityPortal> reference;

    public PortalInactiveSound(EntityPortal portal) {
        super(ModSounds.PORTAL_CLOSED_ACTIVE, SoundCategory.PLAYERS);
        this.reference = new WeakReference<>(portal);
        this.xPosF = (float) portal.posX;
        this.yPosF = (float) portal.posY;
        this.zPosF = (float) portal.posZ;
        this.repeat = true;
    }

    @Override
    public void update() {
        EntityPortal entity = reference.get();
        donePlaying = entity == null || entity.isDead || entity.isOpen();
    }

    @Override
    public float getVolume() {
        EntityPortal entity = reference.get();
        return super.getVolume() * (entity == null ? 0F : 1F - ((float) entity.tick / (float) entity.getLifeTime()));
    }
}
