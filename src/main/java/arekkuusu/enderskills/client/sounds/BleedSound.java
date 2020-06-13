package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.Optional;

@SideOnly(Side.CLIENT)
public class BleedSound extends MovingSound {

    public final WeakReference<EntityLivingBase> reference;

    public BleedSound(EntityLivingBase entity) {
        super(ModSounds.BLEED_ACTIVE, SoundCategory.PLAYERS);
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
            if (SkillHelper.isActiveNotOwner(entity, ModAbilities.BLEED)) {
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
        float[] data = new float[2];
        EntityLivingBase entity = reference.get();
        if (entity != null && !entity.isDead) {
            SkillHelper.getActive(entity, ModAbilities.BLEED, holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (entity != user) {
                        data[0] += holder.tick;
                        data[1] += holder.data.time;
                    }
                });
            });
        }
        return data[0] / data[1];
    }
}
