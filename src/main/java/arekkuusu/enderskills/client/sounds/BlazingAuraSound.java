package arekkuusu.enderskills.client.sounds;

import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class BlazingAuraSound extends MovingSound {

    public final NoiseGeneratorPerlin noiseGeneratorPerlin = new NoiseGeneratorPerlin(new Random(), 1);
    public final WeakReference<EntityLivingBase> reference;
    public int noiseTick;
    public int tick = 10;

    public BlazingAuraSound(EntityLivingBase entity) {
        super(ModSounds.BLAZING_AURA_ACTIVE, SoundCategory.PLAYERS);
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
            if (SkillHelper.isActiveFrom(entity, ModAbilities.BLAZING_AURA)) {
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
        noiseTick++;
    }

    @Override
    public float getPitch() {
        return MathHelper.clamp((float) noiseGeneratorPerlin.getValue(super.getPitch(), noiseTick), 0.2F, 1F);
    }


    @Override
    public float getVolume() {
        return super.getVolume() * getProgress();
    }

    public float getProgress() {
        return tick / 10F;
    }
}
