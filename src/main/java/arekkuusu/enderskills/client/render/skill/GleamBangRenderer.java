package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGleamFlash;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.light.GleamBang;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GleamBangRenderer extends SkillRenderer<GleamBang> {

    private static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/glowing.png");

    public GleamBangRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.GLEAM_BANG, ProjectileLightRenderer::new);
        MinecraftForge.EVENT_BUS.register(new FogRenderer.Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onFogDensityRender(EntityViewRenderEvent.FogDensity event) {
            if (!SkillHelper.isActive(event.getEntity(), ModEffects.BLINDED) && SkillHelper.isActive(event.getEntity(), ModAbilities.GLEAM_BANG)) {
                GlStateManager.setFog(GlStateManager.FogMode.EXP);
                event.setDensity(1F);
                event.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableGleamFlash> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableGleamFlash entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            int tick = Math.min(entity.tick, EntityPlaceableData.MIN_TIME);
            double scale = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", (1F - (float) tick / (float) entity.getLifeTime()));
            }
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            this.bindTexture(getEntityTexture(entity));
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();

            buffer.pos(scale, -scale, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(-scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(0, 0).endVertex();

            buffer.pos(-scale, -scale, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 0).endVertex();

            buffer.pos(scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, -scale).tex(0, 0).endVertex();

            buffer.pos(scale, scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 0).endVertex();

            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableGleamFlash entity) {
            return PLACEABLE;
        }
    }
}
