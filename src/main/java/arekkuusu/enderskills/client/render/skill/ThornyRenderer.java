package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
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
        ShaderLibrary.BRIGHT.begin();
        ShaderLibrary.BRIGHT.set("alpha", SkillRenderer.getBlend(skillHolder.tick, skillHolder.data.time, 1F));
        GlStateManager.disableLighting();
        this.bindTexture(FOLLOWING);
        double width = entity.width / 2;
        double height = entity.height;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        double u = entity.width / 1D;
        double v = entity.height / 1D;
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(width, 0, -width).tex(u, 0).endVertex();
        buffer.pos(width, height, -width).tex(u, v).endVertex();
        buffer.pos(-width, height, -width).tex(0, v).endVertex();
        buffer.pos(-width, height, -width).tex(0, v).endVertex();
        buffer.pos(width, height, -width).tex(u, v).endVertex();
        buffer.pos(width, 0, -width).tex(u, 0).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();

        buffer.pos(width, 0, width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, width).tex(u, 0).endVertex();
        buffer.pos(-width, height, width).tex(u, v).endVertex();
        buffer.pos(width, height, width).tex(0, v).endVertex();
        buffer.pos(width, height, width).tex(0, v).endVertex();
        buffer.pos(-width, height, width).tex(u, v).endVertex();
        buffer.pos(-width, 0, width).tex(u, 0).endVertex();
        buffer.pos(width, 0, width).tex(0, 0).endVertex();

        buffer.pos(-width, 0, width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, -width).tex(u, 0).endVertex();
        buffer.pos(-width, height, -width).tex(u, v).endVertex();
        buffer.pos(-width, height, width).tex(0, v).endVertex();
        buffer.pos(-width, height, width).tex(0, v).endVertex();
        buffer.pos(-width, height, -width).tex(u, v).endVertex();
        buffer.pos(-width, 0, -width).tex(u, 0).endVertex();
        buffer.pos(-width, 0, width).tex(0, 0).endVertex();

        buffer.pos(width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(width, 0, width).tex(u, 0).endVertex();
        buffer.pos(width, height, width).tex(u, v).endVertex();
        buffer.pos(width, height, -width).tex(0, v).endVertex();
        buffer.pos(width, height, -width).tex(0, v).endVertex();
        buffer.pos(width, height, width).tex(u, v).endVertex();
        buffer.pos(width, 0, width).tex(u, 0).endVertex();
        buffer.pos(width, 0, -width).tex(0, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableLighting();
        ShaderLibrary.BRIGHT.end();
        GlStateManager.popMatrix();
    }
}
