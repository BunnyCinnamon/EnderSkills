package arekkuusu.enderskills.client.render.effect;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleVanilla extends ParticleBase {

    public boolean isAdditive;
    public boolean noFading;
    public int index;

    public ParticleVanilla(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, int index) {
        super(world, pos, speed, scale, age, rgb, null);
        this.setParticleTextureIndex(index);
        this.index = index;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (noFading) {
            particleAlpha = 1F;
        }
        if (index != 48) {
            this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        }
    }

    @Override
    public boolean isAdditive() {
        return isAdditive;
    }

    @Override
    public int getFXLayer() {
        return 0;
    }

    @Override
    public int getBrightnessForRender(float p_189214_1_) {
        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
        return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, 0) : 0;
    }
}
