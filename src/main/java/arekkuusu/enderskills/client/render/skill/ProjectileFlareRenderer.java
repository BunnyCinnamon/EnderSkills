package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ProjectileFlareRenderer extends Render<EntityThrowableData> {

    protected ProjectileFlareRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float particleProgress = Math.min(((float) entity.ticksExisted / 20F), 1F);
        float particleScale = 0.5F + 2F * particleProgress;
        for (int i = 0; i < 3; i++) {
            if (ClientProxy.canParticleSpawn()) {
                Vector posVec = RenderMisc.getPositionVectorWithPartialTicks(entity, partialTicks);
                Vector speedVec = Vector.Right.rotateRandom(entity.world.rand, 360F).multiply(0.025D * entity.world.rand.nextDouble());
                EnderSkills.getProxy().spawnParticle(entity.world, posVec.toVec3d(), speedVec.toVec3d(), particleScale, 10, 0xFFFFFF, ResourceLibrary.GLOW);
            }
        }

        if (entity.ticksExisted % 2 == 0 && entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
            Vector posVec = RenderMisc.getPositionVectorWithPartialTicks(entity, partialTicks);
            Vector speedVec = Vector.Right.rotateRandom(entity.world.rand, 360F).multiply(0.025D * entity.world.rand.nextDouble());
            entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posVec.x, posVec.y, posVec.z, speedVec.x, speedVec.y, speedVec.z);
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
