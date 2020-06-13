package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.Fireball;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        Entity owner = NBTHelper.getEntity(EntityLivingBase.class, skillHolder.data.nbt, "user");
        if (owner != entity) {
            RenderMisc.renderEntityOnFire(entity, x, y, z);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            double scale = entity.getRadius() * 2 * MathHelper.clamp(entity.tick / 5D, 0D, 1D);
            if (entity.tick % 2 == 0) {
                for (int i = 0; i < 4; i++) {
                    if (entity.world.rand.nextDouble() < 0.8D) {
                        Vec3d vec = entity.getPositionVector();
                        double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                        double posY = vec.y + scale * (entity.world.rand.nextDouble() - 0.5);
                        double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                        entity.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
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

    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            for (int i = 0; i < 6; i++) {
                if (entity.world.rand.nextDouble() < 0.6D) {
                    Vec3d vec = entity.getPositionEyes(1F);
                    Vec3d motion = new Vec3d(entity.prevPosX, entity.prevPosY + entity.getEyeHeight(), entity.prevPosZ).subtract(vec).scale(0.15F);
                    double offset = entity.world.rand.nextDouble();
                    double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.x * offset;
                    double posY = vec.y + (entity.height / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.y * offset;
                    double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.z * offset;
                    EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 10F, 5, 0xFFE077, ResourceLibrary.GLOW_PARTICLE_EFFECT);
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
