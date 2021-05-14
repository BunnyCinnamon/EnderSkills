package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class ProjectileElectricRenderer extends Render<EntityThrowableData> {

    protected ProjectileElectricRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.world.rand.nextDouble() < 0.3D && ClientProxy.canParticleSpawn()) {
            for (int i = 1; i <= 3; i++) {
                Vector posVec = RenderMisc.getPerpendicularPositionVectorWithPartialTicks(entity, partialTicks, i);
                EnderSkills.getProxy().spawnParticle(entity.world, posVec.toVec3d(), new Vec3d(0, 0, 0), 3F, 25, 0xFFECA8, ResourceLibrary.MOTE);
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
        return null;
    }
}
