package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blackflame.BlackFlameBall;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BlackFlameBallRenderer extends SkillRenderer<BlackFlameBall> {

    public BlackFlameBallRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.BLACK_FLAME_BALL, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.BLACK_FLAME_BALL, Projectile::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            double scale = entity.getRadius() * 2 * MathHelper.clamp(entity.tick / 5D, 0D, 1D);
            if (entity.tick % 3 == 0 && entity.world.rand.nextDouble() < 0.6D && ClientProxy.canParticleSpawn()) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + scale * (entity.world.rand.nextDouble() - 0.5);
                double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d((1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05), 2F, 10, 0xFFFFFF, ResourceLibrary.GLOW_NEGATIVE);
            }
            if (entity.tick % 5 == 0 && entity.world.rand.nextDouble() < 0.4D && ClientProxy.canParticleSpawn()) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + scale * (entity.world.rand.nextDouble() - 0.5);
                double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                entity.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, (1D - 2D * entity.world.rand.nextDouble()) * 0.05);
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }

    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            float particleProgress = Math.min((((float) entity.ticksExisted + partialTicks) / 5F), 1F);
            float particleScale = 0.5F + 10F * particleProgress;

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
