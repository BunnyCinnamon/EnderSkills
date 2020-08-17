package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Thorny;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ThornyRenderer extends SkillRenderer<Thorny> {

    private static final ResourceLocation FOLLOWING = new ResourceLocation(LibMod.MOD_ID, "textures/entity/thorny.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.begin();
            ShaderLibrary.ALPHA.set("alpha", SkillRenderer.getDiffuseBlend(skillHolder.tick, skillHolder.data.time, 1F));
        }
        GlStateManager.disableLighting();
        this.bindTexture(FOLLOWING);
        double width = entity.width / 2 + 0.1;
        double height = entity.height + 0.1;
        for (int i = 0; i < 2; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(45F * i, 0F, 1F, 0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            double u = entity.width / 1D;
            double v = entity.height / 1D;
            buffer.pos(-width, -0.1, -width).tex(0, 0).endVertex();
            buffer.pos(width, -0.1, -width).tex(u, 0).endVertex();
            buffer.pos(width, height, -width).tex(u, v).endVertex();
            buffer.pos(-width, height, -width).tex(0, v).endVertex();
            buffer.pos(-width, height, -width).tex(0, v).endVertex();
            buffer.pos(width, height, -width).tex(u, v).endVertex();
            buffer.pos(width, 0, -width).tex(u, 0).endVertex();
            buffer.pos(-width, 0, -width).tex(0, 0).endVertex();

            buffer.pos(width, -0.1, width).tex(0, 0).endVertex();
            buffer.pos(-width, -0.1, width).tex(u, 0).endVertex();
            buffer.pos(-width, height, width).tex(u, v).endVertex();
            buffer.pos(width, height, width).tex(0, v).endVertex();
            buffer.pos(width, height, width).tex(0, v).endVertex();
            buffer.pos(-width, height, width).tex(u, v).endVertex();
            buffer.pos(-width, -0.1, width).tex(u, 0).endVertex();
            buffer.pos(width, -0.1, width).tex(0, 0).endVertex();

            buffer.pos(-width, -0.1, width).tex(0, 0).endVertex();
            buffer.pos(-width, -0.1, -width).tex(u, 0).endVertex();
            buffer.pos(-width, height, -width).tex(u, v).endVertex();
            buffer.pos(-width, height, width).tex(0, v).endVertex();
            buffer.pos(-width, height, width).tex(0, v).endVertex();
            buffer.pos(-width, height, -width).tex(u, v).endVertex();
            buffer.pos(-width, -0.1, -width).tex(u, 0).endVertex();
            buffer.pos(-width, -0.1, width).tex(0, 0).endVertex();

            buffer.pos(width, -0.1, -width).tex(0, 0).endVertex();
            buffer.pos(width, -0.1, width).tex(u, 0).endVertex();
            buffer.pos(width, height, width).tex(u, v).endVertex();
            buffer.pos(width, height, -width).tex(0, v).endVertex();
            buffer.pos(width, height, -width).tex(0, v).endVertex();
            buffer.pos(width, height, width).tex(u, v).endVertex();
            buffer.pos(width, -0.1, width).tex(u, 0).endVertex();
            buffer.pos(width, -0.1, -width).tex(0, 0).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }
        GlStateManager.enableLighting();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.end();
        }
        GlStateManager.popMatrix();
    }
}
