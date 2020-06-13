package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.common.entity.EntityTokenOrb;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTokenOrb extends Render<EntityTokenOrb> {

    private static final ResourceLocation EXPERIENCE_ORB_TEXTURES = new ResourceLocation("textures/entity/experience_orb.png");

    public RenderTokenOrb(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    public void doRender(EntityTokenOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!this.renderOutlines) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            this.bindEntityTexture(entity);
            RenderHelper.enableStandardItemLighting();
            int i = entity.getTextureByTokens();
            float f = (float) (i % 4 * 16) / 64.0F;
            float f1 = (float) (i % 4 * 16 + 16) / 64.0F;
            float f2 = (float) (i / 4 * 16) / 64.0F;
            float f3 = (float) (i / 4 * 16 + 16) / 64.0F;
            int j = entity.getBrightnessForRender();
            int k = j % 65536;
            int l = j / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float f9 = ((float) entity.color + partialTicks) / 2.0F;
            l = (int) ((MathHelper.sin(f9 + 0.0F) + 1.0F) * 0.5F * 255.0F);
            int j1 = (int) ((MathHelper.sin(f9 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
            GlStateManager.translate(0.0F, 0.1F, 0.0F);
            GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.3F, 0.3F, 0.3F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            bufferbuilder.pos(-0.5D, -0.25D, 0.0D).tex(f, f3).color(l, 255, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(0.5D, -0.25D, 0.0D).tex(f1, f3).color(l, 255, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(0.5D, 0.75D, 0.0D).tex(f1, f2).color(l, 255, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(-0.5D, 0.75D, 0.0D).tex(f, f2).color(l, 255, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Override
    public ResourceLocation getEntityTexture(EntityTokenOrb entity) {
        return EXPERIENCE_ORB_TEXTURES;
    }
}