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
public class FireSpiritSound2 extends MovingSound {

    public final NoiseGeneratorPerlin noiseGeneratorPerlin = new NoiseGeneratorPerlin(new Random(), 1);
    public final WeakReference<EntityLivingBase> reference;
    public int tick;

    public FireSpiritSound2(EntityLivingBase entity) {
        super(ModSounds.FIRE_SPIRIT_ACTIVE2, SoundCategory.PLAYERS);
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
            if (SkillHelper.isActiveOwner(entity, ModAbilities.FIRE_SPIRIT)) {
                this.xPosF = (float) entity.posX;
                this.yPosF = (float) entity.posY;
                this.zPosF = (float) entity.posZ;
            } else {
                donePlaying = true;
            }
        } else {
            donePlaying = true;
        }
        tick++;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * (1F - getProgress());
    }

    @Override
    public float getPitch() {
        return MathHelper.clamp((float) noiseGeneratorPerlin.getValue(super.getPitch(), tick), 0.2F, 1F);
    }

    public float getProgress() {
        float[] data = new float[1];
        EntityLivingBase entity = reference.get();
        if (entity != null && !entity.isDead) {
            SkillHelper.getActiveOwner(entity, ModAbilities.FIRE_SPIRIT, h -> data[0] = (float) h.tick / (float) h.data.time);
        }
        return data[0];
    }
}
