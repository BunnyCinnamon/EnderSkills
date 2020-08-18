package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class ProjectileVoid extends Render<EntityThrowableData> {

    public final int[] colors = new int[]{
            0x1E0034,
            0x260742,
            0x30104F,
            0x38185B,
            0x401E68,
            0x472476,
            0x4E2A84,
            0x5B3C8C,
            0x684C96,
            0x765D9F,
            0x836EA9,
    };

    protected ProjectileVoid(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.world.rand.nextDouble() < 0.3D && ClientProxy.canParticleSpawn()) {
            Vector vec = new Vector(entity.getPositionEyes(1F));
            Vector motion = new Vector(entity.motionX, entity.motionY, entity.motionZ).multiply(-1);
            double offset = entity.world.rand.nextDouble();

            for (int i = 1; i <= 3; i++) {
                Vector posVec = motion.normalize()
                        .perpendicular().normalize()
                        .rotate(Quat.fromAxisAngleRad(motion.normalize(), (entity.ticksExisted + partialTicks + i + 1F) * 90F * (float) Math.PI / 180F)).normalize()
                        .add(vec.add(motion.multiply(offset)));
                EnderSkills.getProxy().spawnParticle(entity.world, posVec.toVec3d(), new Vec3d(0, 0, 0), 3F, 25, colors[entity.world.rand.nextInt(colors.length - 1)], ResourceLibrary.MOTE);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
