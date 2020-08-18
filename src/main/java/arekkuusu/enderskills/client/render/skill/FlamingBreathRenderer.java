package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FlamingBreath;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class FlamingBreathRenderer extends SkillRenderer<FlamingBreath> {

    public FlamingBreathRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.FLAMING_BREATH, Placeable::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 2 == 0) {
                Vector vec = new Vector(entity.getLookVec()).normalize();
                double distance = entity.getRadius();
                double posX = entity.posX;
                double posY = entity.posY;
                double posZ = entity.posZ;

                for (int j = 1; j < 2; ++j) {
                    if (ClientProxy.canParticleSpawn()) {
                        Vector speedVec = vec.rotateRandom(entity.world.rand, 80F).multiply(distance * j / 3D);
                        Vector posVec = vec.rotateRandom(entity.world.rand, 80F).multiply(distance * entity.world.rand.nextDouble()).addVector(posX, posY, posZ);
                        speedVec = new Vector(speedVec.x / 10, speedVec.y / 10, speedVec.z / 10);
                        EnderSkills.getProxy().spawnParticleLuminescence(entity.world, posVec.toVec3d(), speedVec.toVec3d(), 15F, 25, ResourceLibrary.GLOW);
                    }
                }
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
