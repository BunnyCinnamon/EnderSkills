package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.ES;
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
        for (int i = 0; i < 6; i++) {
            if (entity.world.rand.nextDouble() < 0.6D) {
                Vec3d vec = entity.getPositionEyes(1F);
                Vec3d motion = new Vec3d(entity.prevPosX, entity.prevPosY + entity.getEyeHeight(), entity.prevPosZ).subtract(vec);
                double offset = entity.world.rand.nextDouble();
                double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.x * offset;
                double posY = vec.y + (entity.height / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.y * offset;
                double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.z * offset;
                motion = new Vec3d(0, 0, 0);
                ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), motion, 0.6F, 25, colors[entity.world.rand.nextInt(colors.length - 1)], ResourceLibrary.GLOW_PARTICLE_EFFECT);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
