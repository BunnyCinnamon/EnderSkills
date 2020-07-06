package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableUpdraft;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Updraft;
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
public class UpdraftRenderer extends SkillRenderer<Updraft> {

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick < 10 && skillHolder.tick % 2 == 0 && entity.world.rand.nextDouble() < 0.4D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;

            Vec3d vector = new Vec3d(0, 1, 0);
            double distance = NBTHelper.getDouble(skillHolder.data.nbt, "force");
            Vec3d from = new Vec3d(posX, posY, posZ);
            Vec3d to = from.addVector(
                    vector.x * distance,
                    vector.y * distance,
                    vector.z * distance
            );
            Vec3d difference = to.subtract(from);
            Vec3d motion = new Vec3d(difference.x / 5D, difference.y / 5D, difference.z / 5D).scale(-1);
            ParticleVanilla vanilla = new ParticleVanilla(entity.world, from, motion, 5F, 18, 0xFFFFFF, 0);
            Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableUpdraft> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableUpdraft entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 2 == 0) {
                Vec3d vec = new Vec3d(entity.width / 2, 0, entity.width / 2);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
                vec = vec.rotateYaw(60 * (float) Math.PI / 180F);
                spawnParticle(vec, entity);
            }
        }

        public void spawnParticle(Vec3d vec, Entity entity) {
            float angle = entity.ticksExisted * 10F % 360F;
            vec = vec.rotateYaw(angle * (float) Math.PI / 180F);
            vec = vec.add(entity.getPositionVector());
            for (int i = 1; i < 4; i++) {
                ParticleVanilla vanilla = new ParticleVanilla(entity.world, vec, new Vec3d(0, 0.3 * i / 3, 0), 8F, 25, 0xFFFFFF, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableUpdraft entity) {
            return null;
        }
    }
}
