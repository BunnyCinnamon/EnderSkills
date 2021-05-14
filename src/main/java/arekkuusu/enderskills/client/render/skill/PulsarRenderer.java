package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.effect.Pulsar;
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
public class PulsarRenderer extends SkillRenderer<Pulsar> {

    public PulsarRenderer() {
        EntityPlaceableDataRenderer.add(ModEffects.PULSAR, Placeable::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            double scale = entity.getRadius() * MathHelper.clamp((entity.tick + partialTicks) / 5D, 0D, 1D);
            if (entity.tick % 2 == 0) {
                //Top
                for (int i = 0; i < 6; i++) {
                    if (entity.world.rand.nextDouble() < 0.8D && ClientProxy.canParticleSpawn()) {
                        Vec3d motionVec = new Vector(0, scale, 0).rotateRandom(entity.world.rand, 110).toVec3d();
                        Vec3d vec = entity.getPositionVector().add(motionVec);
                        motionVec = motionVec.normalize().scale(0.5);
                        float particleScale = 3F + 2F * (float) entity.world.rand.nextGaussian();
                        EnderSkills.getProxy().spawnParticleLuminescence(entity.world, vec, motionVec, particleScale, 15, ResourceLibrary.GLOW);
                    }
                }
                //Middle
                for (int i = 0; i < 6; i++) {
                    if (entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
                        Vec3d motionVec = new Vector(scale, 0, 0).rotateRandomXZ(entity.world.rand, 40).toVec3d();
                        Vec3d vec = entity.getPositionVector().add(motionVec);
                        motionVec = motionVec.normalize().scale(0.5);
                        float particleScale = 3F + 2F * (float) entity.world.rand.nextGaussian();
                        EnderSkills.getProxy().spawnParticleLuminescence(entity.world, vec, motionVec, particleScale, 15, ResourceLibrary.GLOW);
                    }
                }
                //Bottom
                for (int i = 0; i < 6; i++) {
                    if (entity.world.rand.nextDouble() < 0.8D && ClientProxy.canParticleSpawn()) {
                        Vec3d motionVec = new Vector(0, -scale, 0).rotateRandom(entity.world.rand, 110).toVec3d();
                        Vec3d vec = entity.getPositionVector().add(motionVec);
                        motionVec = motionVec.normalize().scale(0.5);
                        float particleScale = 3F + 2F * (float) entity.world.rand.nextGaussian();
                        EnderSkills.getProxy().spawnParticleLuminescence(entity.world, vec, motionVec, particleScale, 15, ResourceLibrary.GLOW);
                    }
                }
                //Smoke
                for (int i = 0; i < 4; i++) {
                    if (entity.world.rand.nextDouble() < 0.8D && ClientProxy.canParticleSpawn()) {
                        Vec3d motionVec = new Vector(0, scale, 0).rotateRandom(entity.world.rand, 360).toVec3d();
                        Vec3d vec = entity.getPositionVector().add(motionVec);
                        motionVec = motionVec.normalize().scale(0.1);
                        entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, vec.x, vec.y, vec.z, motionVec.x, motionVec.y, motionVec.z);
                    }
                }
            }
        }

        @Override
        @Nullable
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
