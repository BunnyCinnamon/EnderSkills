package arekkuusu.enderskills.client.render.effect;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleLuminescence extends ParticleBase {

    private final float initialG;
    private final float initialB;

    public ParticleLuminescence(World world, Vec3d pos, Vec3d speed, float scale, int age, ResourceLocation location) {
        super(world, pos, speed, scale, age, 0xFFE077, location);
        this.initialG = getGreenColorF();
        this.initialB = getBlueColorF();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        float life = (float) particleAge / (float) particleMaxAge;
        particleGreen = initialG - 0.75F * life;
        particleBlue = initialB - 0.45F * life;
    }
}
