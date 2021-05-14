package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.SpriteLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.sprite.UVFrame;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.electric.ElectricPulse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ElectricPulseRenderer extends SkillRenderer<ElectricPulse> {

    private static final ResourceLocation GLINT = new ResourceLocation(LibMod.MOD_ID, "textures/entity/shocking_aura.png");

    public ElectricPulseRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.ELECTRIC_PULSE, Placeable::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            int tick = Math.min(entity.tick, EntityPlaceableData.MIN_TIME);
            double scale = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            renderAuraFence(x, y, z, scale, entity.tick, partialTicks);
            renderAura(x, y, z, scale, entity.tick, Minecraft.getSystemTime(), (tick / (float) EntityPlaceableData.MIN_TIME));
        }

        public void renderAuraFence(double x, double y, double z, double scale, float tick, float partialTicks) {
            //Render electricity
            GlStateManager.pushMatrix();
            GlStateManager.color(0.608F, 0.508F, 0.19F, 1F);
            SpriteLibrary.ELECTRIC_RING.bind();
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 1F);
            }
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.translate(x, y, z);
            float y1 = 0F + (((tick + partialTicks) % 10F) / 10F) * 0.5F;
            renderFence(0F, (float) scale + (((tick + partialTicks) % 10F) / 10F) * 0.5F, 0, tick, partialTicks, 1.5F, 3, scale);
            renderFence(0F, (float) scale + (((tick + partialTicks) % 10F) / 10F) * 0.5F, 0, tick, partialTicks, 3F, 6, scale);
            renderFence(0F, y1, 0, tick, partialTicks, 1.5F, 2, scale);
            renderFence(0F, y1, 0, tick, partialTicks, 2.5F, 4, scale);
            renderFence(0F, -(float) scale + (((tick + partialTicks) % 10F) / 10F) * 0.5F, 0, tick, partialTicks, 1.5F, 1, scale);
            renderFence(0F, -(float) scale + (((tick + partialTicks) % 10F) / 10F) * 0.5F, 0, tick, partialTicks, 3F, 6, scale);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
        }

        public void renderAura(double x, double y, double z, double scale, float tick, long systemTime, float alpha) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            //Render cube
            GlStateManager.pushMatrix();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.pushMatrix();
            GlStateManager.color(0.608F, 0.508F, 0.19F, alpha);
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            //GlStateManager.scale(0.25F, 0.25F, 0.25F);
            float i = (systemTime % 5000L) / 5000.0F * 6.0F;
            GlStateManager.translate(i, 0.0F, 0.0F);
            //GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            hahaFunnyRenderFunction(x, y, z, scale);
            GlStateManager.popMatrix();
            //Render cube
            GlStateManager.pushMatrix();
            GlStateManager.color(0.608F, 0.508F, 0.19F, alpha);
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            //GlStateManager.scale(0.25F, 0.25F, 0.25F);
            float i0 = (systemTime % 6873L) / 6873.0F * 6.0F;
            GlStateManager.translate(-i0, 0.0F, 0.0F);
            //GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            hahaFunnyRenderFunction(x, y, z, scale);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        public void renderFence(float x, float y, float z, float tick, float partialTicks, float angleOffset, int textureOffset, double scale) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            for (int i = 0; i < 4; i++) {
                UVFrame frame = SpriteLibrary.ELECTRIC_RING.getFrame(tick + textureOffset + partialTicks + i);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(((360F / 4F) * i) + (tick * angleOffset + partialTicks) % 360, 0F, 1F, 0F);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-scale, -scale, -scale).tex(frame.uMin, frame.vMin).endVertex();
                buffer.pos(scale, -scale, -scale).tex(frame.uMax, frame.vMin).endVertex();
                buffer.pos(scale, scale, -scale).tex(frame.uMax, frame.vMax).endVertex();
                buffer.pos(-scale, scale, -scale).tex(frame.uMin, frame.vMax).endVertex();
                buffer.pos(-scale, scale, -scale).tex(frame.uMin, frame.vMax).endVertex();
                buffer.pos(scale, scale, -scale).tex(frame.uMax, frame.vMax).endVertex();
                buffer.pos(scale, -scale, -scale).tex(frame.uMax, frame.vMin).endVertex();
                buffer.pos(-scale, -scale, -scale).tex(frame.uMin, frame.vMin).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        }

        public void hahaFunnyRenderFunction(double x, double y, double z, double scale) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180, 1F, 0, 0);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            double height = scale;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-scale, -height, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -height, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, height, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, height, -scale).tex(0, 1).endVertex();
            buffer.pos(-scale, height, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, height, -scale).tex(1, 1).endVertex();
            buffer.pos(scale, -height, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -height, -scale).tex(0, 0).endVertex();

            buffer.pos(scale, -height, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -height, scale).tex(1, 0).endVertex();
            buffer.pos(-scale, height, scale).tex(1, 1).endVertex();
            buffer.pos(scale, height, scale).tex(0, 1).endVertex();
            buffer.pos(scale, height, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, height, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -height, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -height, scale).tex(0, 0).endVertex();

            buffer.pos(-scale, -height, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -height, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, height, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, height, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, height, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, height, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -height, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -height, scale).tex(0, 0).endVertex();

            buffer.pos(scale, -height, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -height, scale).tex(1, 0).endVertex();
            buffer.pos(scale, height, scale).tex(1, 1).endVertex();
            buffer.pos(scale, height, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, height, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, height, scale).tex(1, 1).endVertex();
            buffer.pos(scale, -height, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -height, -scale).tex(0, 0).endVertex();
            tessellator.draw();

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return GLINT;
        }
    }
}
