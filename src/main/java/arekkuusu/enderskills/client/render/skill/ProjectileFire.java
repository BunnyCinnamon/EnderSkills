package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class ProjectileFire extends Render<EntityThrowableData> {

    protected ProjectileFire(RenderManager renderManager) {
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
                entity.world.spawnParticle(EnumParticleTypes.FLAME, posVec.x, posVec.y, posVec.z, 0, 0, 0);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
