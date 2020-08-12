package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Slash;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class SlashRenderer extends SkillRenderer<Slash> {

    public static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/slash.png");

    public SlashRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.SLASH, Placeable::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick > entity.getLifeTime()) return;
            double progress = ((double) entity.tick + partialTicks) / (double) entity.getLifeTime();
            double scale = (entity.getRadius() + 3) * 2 * progress;
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.BRIGHT.begin();
                ShaderLibrary.BRIGHT.set("alpha", 0.6F * (1F - (float) progress));
            }
            for (int i = -1; i <= 1; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y, z);
                GlStateManager.scale(scale, scale, scale);
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(entity.rotationYaw, 0F, -1F, 0F);
                    GlStateManager.rotate((entity.rotationPitch + 12 * i) - 35F, 1F, 0F, 0F);
                    this.bindTexture(getEntityTexture(entity));
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                    double yOffset = -(0.5D - 0.25D * progress);
                    buffer.pos(-0.5D, yOffset, -0.5D).tex(0, 0).endVertex();
                    buffer.pos(0.5D, yOffset, -0.5D).tex(1, 0).endVertex();
                    buffer.pos(0.5D, yOffset, 0.5D).tex(1, 1).endVertex();
                    buffer.pos(-0.5D, yOffset, 0.5D).tex(0, 1).endVertex();

                    buffer.pos(-0.5D, yOffset, 0.5D).tex(0, 1).endVertex();
                    buffer.pos(0.5D, yOffset, 0.5D).tex(1, 1).endVertex();
                    buffer.pos(0.5D, yOffset, -0.5D).tex(1, 0).endVertex();
                    buffer.pos(-0.5D, yOffset, -0.5D).tex(0, 0).endVertex();
                    tessellator.draw();
                    GlStateManager.popMatrix();
                }
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.BRIGHT.end();
            }
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return PLACEABLE;
        }
    }
}
