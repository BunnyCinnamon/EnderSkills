package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FlamingBreath;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class FlamingBreathRenderer extends SkillRenderer<FlamingBreath> {

    public FlamingBreathRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.FLAMING_BREATH, Placeable::new);
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
                Vector vec = new Vector(entity.getLookVec()).normalize();
                double distance = entity.getRadius();
                double posX = entity.posX;
                double posY = entity.posY;
                double posZ = entity.posZ;

                for (int i = 0; i < 2; ++i) {
                    for (int j = 1; j < 3; ++j) {
                        Vector speedVec = vec.rotateRandom(entity.world.rand, 80F).multiply(distance * j / 3);
                        speedVec = new Vector(speedVec.x / 10, speedVec.y / 10, speedVec.z / 10);
                        entity.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, speedVec.x, speedVec.y, speedVec.z);
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
