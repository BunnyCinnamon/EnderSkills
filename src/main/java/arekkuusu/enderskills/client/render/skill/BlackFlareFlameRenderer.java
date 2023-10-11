package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blackflame.BlackFlareFlame;
import arekkuusu.enderskills.common.skill.ability.offence.blackflame.BlackScouringFlame;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BlackFlareFlameRenderer extends SkillRenderer<BlackFlareFlame> {

    public BlackFlareFlameRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.BLACK_FLARE_FLAME, Projectile::new);
    }

    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            float particleProgress = Math.min((((float) entity.ticksExisted + partialTicks) / 5F), 1F);
            float particleScale = 0.05F + 0.2F * particleProgress;

            for (int i = 0; i < 5; i++) {
                if (ClientProxy.canParticleSpawn()) {
                    Vector posVec = RenderMisc.getPositionVectorWithPartialTicks(entity, partialTicks);
                    Vector speedVec = Vector.Right.rotateRandom(entity.world.rand, 360F).multiply(0.025D * entity.world.rand.nextDouble());
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, posVec.toVec3d(), speedVec.toVec3d(), particleScale, 10, ResourceLibrary.GLOW_NEGATIVE, 0xFFFFFF);
                }
            }

            if (entity.ticksExisted % 2 == 0 && entity.world.rand.nextDouble() < 0.8D && ClientProxy.canParticleSpawn()) {
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
}
