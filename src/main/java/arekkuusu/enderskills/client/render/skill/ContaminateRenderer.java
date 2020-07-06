package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blood.Contaminate;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ContaminateRenderer extends SkillRenderer<Contaminate> {

    public ContaminateRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.CONTAMINATE, Projectile::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 5 == 0 && entity.world.rand.nextDouble() < 0.8D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 2.5F, 50, 0xBED33D, ResourceLibrary.SKULL);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            for (int i = 0; i < 6; i++) {
                if (entity.world.rand.nextDouble() < 0.6D) {
                    Vec3d vec = entity.getPositionEyes(1F);
                    Vec3d motion = new Vec3d(entity.prevPosX, entity.prevPosY + entity.getEyeHeight(), entity.prevPosZ).subtract(vec);
                    double offset = entity.world.rand.nextDouble();
                    double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.x * offset;
                    double posY = vec.y + (entity.height / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.y * offset;
                    double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.z * offset;
                    motion = new Vec3d(0, 0, 0);
                    ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), motion, 1F, 25, 0x690303, ResourceLibrary.SPIT);
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
