package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.client.render.model.ModelStoneGolem;
import arekkuusu.enderskills.common.entity.EntityStoneGolem;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StoneGolemRender extends RenderLiving<EntityStoneGolem> {

    public static final ResourceLocation IRON_GOLEM_TEXTURES = new ResourceLocation(LibMod.MOD_ID, "textures/entity/stone_golem.png");

    public StoneGolemRender(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelStoneGolem(), 0.5F);
    }

    @Override
    protected void renderLivingAt(EntityStoneGolem entityLivingBaseIn, double x, double y, double z) {
        if(entityLivingBaseIn.ticksExisted < entityLivingBaseIn.growTime) {
            GlStateManager.translate(x, y - (2.7F - (entityLivingBaseIn.height / 2.7F) * 2.7F), z);
        } else {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStoneGolem entity) {
        return IRON_GOLEM_TEXTURES;
    }

    @Override
    protected void applyRotations(EntityStoneGolem entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);

        if ((double) entityLiving.limbSwingAmount >= 0.01D) {
            float f1 = entityLiving.limbSwing - entityLiving.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
            float f2 = (Math.abs(f1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            GlStateManager.rotate(6.5F * f2, 0.0F, 0.0F, 1.0F);
        }
    }
}