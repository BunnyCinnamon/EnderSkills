package arekkuusu.enderskills.client.render.effect;

import arekkuusu.enderskills.client.util.ResourceLibrary;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleLuminescence extends ParticleBase {

    private final float initialG;
    private final float initialB;
    private final boolean doit;

    public ParticleLuminescence(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        super(world, pos, speed, scale, age, rgb, location);
        this.initialG = getGreenColorF();
        this.initialB = getBlueColorF();
        this.doit = location == ResourceLibrary.GLOW;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        float life = (float) particleAge / (float) particleMaxAge;
        if (doit) {
            particleGreen = initialG - 0.75F * life;
            particleBlue = initialB - 0.45F * life;
        }
    }

    @Override
    public boolean isAdditive() {
        return doit && super.isAdditive();
    }

    @Override
    public int getBrightnessForRender(float idk) {
        float life = (float) particleAge / (float) particleMaxAge;
        return doit ? super.getBrightnessForRender(idk) : (int) (255 * (1 - life));
    }
}
