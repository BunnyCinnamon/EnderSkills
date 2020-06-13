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
public class SacrificeSound extends MovingSound {

    public final WeakReference<EntityLivingBase> reference;

    public SacrificeSound(EntityLivingBase entity) {
        super(ModSounds.SACRIFICE_ACTIVE, SoundCategory.PLAYERS);
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
            if (SkillHelper.isActiveOwner(entity, ModAbilities.SACRIFICE)) {
                this.xPosF = (float) entity.posX;
                this.yPosF = (float) entity.posY;
                this.zPosF = (float) entity.posZ;
            } else {
                donePlaying = true;
            }
        } else {
            donePlaying = true;
        }
    }

    @Override
    public float getVolume() {
        return super.getVolume() * (1F - getProgress());
    }

    public float getProgress() {
        float[] data = new float[1];
        EntityLivingBase entity = reference.get();
        if (entity != null && !entity.isDead) {
            SkillHelper.getActiveOwner(entity, ModAbilities.SACRIFICE, h -> data[0] = (float) h.tick / (float) h.data.time);
        }
        return data[0];
    }
}
