package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
public class RingOfFireSound extends MovingSound {

    public final WeakReference<EntityPlaceableData> reference;

    public RingOfFireSound(EntityPlaceableData entity) {
        super(ModSounds.RING_OF_FIRE, SoundCategory.PLAYERS);
        this.reference = new WeakReference<>(entity);
        this.xPosF = (float) entity.posX;
        this.yPosF = (float) entity.posY + entity.getRadius() / 2;
        this.zPosF = (float) entity.posZ;
        this.repeat = true;
    }

    @Override
    public void update() {
        EntityPlaceableData entity = reference.get();
        donePlaying = entity == null || entity.isDead;
    }

    @Override
    public float getVolume() {
        EntityPlaceableData entity = reference.get();
        float dist = entity == null ? 0F : (float) Minecraft.getMinecraft().player.getDistance(entity);
        return super.getVolume() * (entity == null ? 0F : 1F - ((float) entity.tick / (float) (entity.getLifeTime() + 20F))) * (1F - MathHelper.clamp(dist / 100F, 0F, 1F));

    }
}
