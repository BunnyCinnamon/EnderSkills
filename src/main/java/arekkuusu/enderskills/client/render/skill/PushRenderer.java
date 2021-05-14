package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Push;
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
public class PushRenderer extends SkillRenderer<Push> {

    public PushRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.PUSH, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.PUSH, ProjectileWindRenderer::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick % 5 == 0 && entity.world.rand.nextDouble() < 0.4D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;

            Vec3d vector = NBTHelper.getVector(skillHolder.data.nbt, "vector").scale(-1);
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
    public static class Placeable extends Render<EntityPlaceableData> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 5 == 0) {
                Vector vec = new Vector(entity.getLookVec()).normalize();
                double distance = entity.getRadius();
                double posX = entity.posX;
                double posY = entity.posY;
                double posZ = entity.posZ;

                for (int j = 1; j < 2; ++j) {
                    Vector speedVec = vec.rotateRandom(entity.world.rand, 60F).multiply(distance * j / 3);
                    speedVec = new Vector(speedVec.x / 5, speedVec.y / 5, speedVec.z / 5);
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(speedVec.x, speedVec.y, speedVec.z), 10F, 25, 0xFFFFFF, 0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
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
