package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FocusFlame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
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
            if (entity.tick % 2 == 0) {
                for (int i = 1; i < 4; i++) {
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, entity.getPositionVector(), new Vec3d(0, 0.4 * i / 3, 0), 10F, 25, 0xFFFFFF, 48);
                    vanilla.setCanCollide(true);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                }
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
            float angle = entity.ticksExisted * 20F % 360F;
            vec = vec.rotateYaw(angle * (float) Math.PI / 180F);
            vec = vec.add(entity.getPositionVector());
            //entity.world.spawnParticle(EnumParticleTypes.FLAME, vec.x, vec.y, vec.z, 0, 0.2, 0);
            ParticleVanilla vanilla = new ParticleVanilla(entity.world, vec, new Vec3d(0, 0.3, 0), 8F, 25, 0xFFFFFF, 48);
            Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
