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
public class ShockingAuraSound extends MovingSound {

    public final WeakReference<EntityLivingBase> reference;
    public int tick = 10;

    public ShockingAuraSound(EntityLivingBase entity) {
        super(ModSounds.SHOCKING_AURA, SoundCategory.PLAYERS);
        this.reference = new WeakReference<>(entity);
        this.xPosF = (float) entity.posX;
        this.yPosF = (float) entity.posY;
        this.zPosF = (float) entity.posZ;
        this.repeat = true;
        this.volume = 0.25F;
    }

    @Override
    public void update() {
        EntityLivingBase entity = reference.get();
        if (entity != null && !entity.isDead) {
            if (SkillHelper.isActive(entity, ModAbilities.SHOCKING_AURA)) {
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
