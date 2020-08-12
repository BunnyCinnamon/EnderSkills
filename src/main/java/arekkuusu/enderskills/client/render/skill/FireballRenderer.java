package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.Fireball;
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
public class FireballRenderer extends SkillRenderer<Fireball> {

    public FireballRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.FIREBALL, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.FIREBALL, Projectile::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            double scale = entity.getRadius() * 2 * MathHelper.clamp(entity.tick / 5D, 0D, 1D);
            if (entity.tick % 5 == 0 && entity.world.rand.nextDouble() < 0.4D && ClientProxy.canParticleSpawn()) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + scale * (entity.world.rand.nextDouble() - 0.5);
                double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                entity.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
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
            float particleProgress = Math.min(((float) entity.ticksExisted / ((float) entity.getLifeTime() / 5F)), 1F);
            float particleScale = 0.5F + 5F * particleProgress;
            for (int i = 0; i < 6; i++) {
                if (ClientProxy.canParticleSpawn()) {
                    Vec3d vec = entity.getPositionEyes(1F);
                    Vec3d motion = new Vec3d(entity.prevPosX, entity.prevPosY + entity.getEyeHeight(), entity.prevPosZ).subtract(vec);
                    double offset = entity.world.rand.nextDouble();
                    double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.x * offset;
                    double posY = vec.y + (entity.height / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.y * offset;
                    double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.z * offset;
                    Vector speedVec = Vector.Right.rotateRandom(entity.world.rand, 360F).multiply(0.025D * entity.world.rand.nextDouble());
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), speedVec.toVec3d(), particleScale, 60, ResourceLibrary.GLOW_PARTICLE_EFFECT);
                }
            }
        }

        @Override
        @Nullable
        protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
            return null;
        }
    }
}
