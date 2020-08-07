package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableFlamingRain;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FlamingRain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class FlamingRainRenderer extends SkillRenderer<FlamingRain> {

    public FlamingRainRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.FLAMING_RAIN, ProjectileFire::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (SkillHelper.isNotOwner(entity, skillHolder)) {
            RenderMisc.renderEntityOnFire(entity, x, y, z);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableFlamingRain> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableFlamingRain entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 2 == 0) {
                for (int i = 0; i < 2; i++) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double posY = vec.y - 0.5 * entity.world.rand.nextDouble();
                    double posZ = vec.z + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    Vec3d spawnVec = new Vec3d(posX, posY, posZ);
                    double distance = spawnVec.distanceTo(vec.addVector(0, -(entity.height + 4D), 0));
                    double speed = 1.4D;
                    int time = (int) (distance / speed);
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, spawnVec, new Vec3d(0, -speed, 0), 5F, time, 0xFFFFFF, 48);
                    vanilla.noFading = true;
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 12F, time, ResourceLibrary.GLOW_PARTICLE_EFFECT);
                }
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableFlamingRain entity) {
            return null;
        }
    }
}
