package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FocusFlame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class FocusFlameRenderer extends SkillRenderer<FocusFlame> {

    public FocusFlameRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.FOCUS_FLAME, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.FOCUS_FLAME, ProjectileFire::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 5 == 0) {
                Vec3d vec = entity.getPositionVector();
                if (entity.world.rand.nextDouble() < 0.6D && ClientProxy.canParticleSpawn()) {
                    double posX = vec.x + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double posY = vec.y - 0.5 * entity.world.rand.nextDouble();
                    double posZ = vec.z + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(motionX, 0.4, motionZ), 12F, 25, ResourceLibrary.GLOW);
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
