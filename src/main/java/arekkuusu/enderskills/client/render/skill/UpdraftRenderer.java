package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableUpdraft;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Updraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UpdraftRenderer extends SkillRenderer<Updraft> {

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableUpdraft> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableUpdraft entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 2 == 0) {
                Vec3d vec = entity.getPositionVector();
                if (entity.world.rand.nextDouble() < 0.6D && ClientProxy.canParticleSpawn()) {
                    double posX = vec.x + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double posY = vec.y - 0.5 * entity.world.rand.nextDouble();
                    double posZ = vec.z + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(motionX, 0.4, motionZ), 12F, 25, 0xFFFFFF, 0);
                    vanilla.setCanCollide(true);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                }
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableUpdraft entity) {
            return null;
        }
    }
}
