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
public class HoverSound extends MovingSound {

    public final WeakReference<EntityLivingBase> reference;

    public HoverSound(EntityLivingBase entity) {
        super(ModSounds.HOVER, SoundCategory.PLAYERS);
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
            if (SkillHelper.isActiveOwner(entity, ModAbilities.HOVER)) {
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
}
