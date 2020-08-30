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
            double offset = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            GlStateManager.color(1F, 1F, 1F, 1F);
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
            renderFence(entity, 0F, (float) scale + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 3, scale);
            renderFence(entity, 0F, (float) scale + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 3F, 6, scale);
            renderFence(entity, 0F, 0F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 2, scale);
            renderFence(entity, 0F, 0F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 2.5F, 4, scale);
            renderFence(entity, 0F, -(float) scale + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 1, scale);
            renderFence(entity, 0F, -(float) scale + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 3F, 6, scale);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
            //Render cube
            GlStateManager.pushMatrix();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.pushMatrix();
            GlStateManager.color(0.608F, 0.508F, 0.19F, 1F *((float) tick / (float) EntityPlaceableData.MIN_TIME));
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            //GlStateManager.scale(0.25F, 0.25F, 0.25F);
            float i = (float) (Minecraft.getSystemTime() % 5000L) / 5000.0F * 6.0F;
            GlStateManager.translate(i, 0.0F, 0.0F);
            //GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            hahaFunnyRenderFunction(x, y, z, scale);
            GlStateManager.popMatrix();
            //Render cube
            GlStateManager.pushMatrix();
            GlStateManager.color(0.608F, 0.508F, 0.19F, 1F * ((float) tick / (float) EntityPlaceableData.MIN_TIME));
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            //GlStateManager.scale(0.25F, 0.25F, 0.25F);
            float i0 = (float) (Minecraft.getSystemTime() % 6873L) / 6873.0F * 6.0F;
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

        public void renderFence(Entity entity, float x, float y, float z, float partialTicks, float angleOffset, int textureOffset, double scale) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            for (int i = 0; i < 4; i++) {
                UVFrame frame = SpriteLibrary.ELECTRIC_RING.getFrame(entity.ticksExisted + textureOffset + partialTicks + i);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(((360F / 4F) * i) + (entity.ticksExisted * angleOffset + partialTicks) % 360, 0F, 1F, 0F);
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
