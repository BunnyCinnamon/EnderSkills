package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class ProjectileWind extends Render<EntityThrowableData> {

    protected ProjectileWind(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.world.rand.nextDouble() < 0.3D && ClientProxy.canParticleSpawn()) {
            Vector vec = new Vector(entity.getPositionEyes(1F));
            Vector motion = new Vector(entity.motionX, entity.motionY, entity.motionZ);
            double offset = entity.world.rand.nextDouble();

            for (int i = 1; i <= 3; i++) {
                Vector posVec = motion.normalize()
                        .perpendicular().normalize()
                        .rotate(Quat.fromAxisAngleRad(motion.normalize(), (entity.ticksExisted + partialTicks + i + 1F) * 90F * (float) Math.PI / 180F)).normalize()
                        .add(vec.add(motion.multiply(offset)));
                ParticleVanilla vanilla = new ParticleVanilla(entity.world, posVec.toVec3d(), new Vec3d(0, 0, 0), 3F, 25, 0xFFFFFF, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
