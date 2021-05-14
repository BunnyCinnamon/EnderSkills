package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
public class WarmHeartSound extends MovingSound {

    public final WeakReference<EntityLivingBase> reference;
    public int tick = 10;

    public WarmHeartSound(EntityLivingBase entity) {
        super(ModSounds.WARM_HEART, SoundCategory.PLAYERS);
        this.reference = new WeakReference<>(entity);
        this.xPosF = (float) entity.posX;
        this.yPosF = (float) entity.posY;
        this.zPosF = (float) entity.posZ;
        this.repeat = true;
    }

    @Override
    public void update() {
        EntityLivingBase entity = reference.get();
        if (entity != null && !entity.isDead) {
            if (SkillHelper.isActiveFrom(entity, ModAbilities.WARM_HEART)) {
                this.xPosF = (float) entity.posX;
                this.yPosF = (float) entity.posY;
                this.zPosF = (float) entity.posZ;
            } else {
                if (--tick < 0) {
                    donePlaying = true;
                }
            }
        } else {
            donePlaying = true;
        }
    }

    @Override
    public float getVolume() {
        return super.getVolume() * getProgress();
    }

    public float getProgress() {
        return tick / 10F;
    }
}
