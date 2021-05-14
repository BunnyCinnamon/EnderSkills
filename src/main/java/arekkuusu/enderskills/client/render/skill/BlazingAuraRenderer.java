package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.defense.fire.BlazingAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlazingAuraRenderer extends SkillRenderer<BlazingAura> {

    private static final ResourceLocation GLINT = new ResourceLocation(LibMod.MOD_ID, "textures/entity/shocking_aura.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        int tick = Math.min(skillHolder.tick, EntityPlaceableData.MIN_TIME);
        double scale = NBTHelper.getDouble(skillHolder.data.nbt, "range") * MathHelper.clamp(((double) tick / 10D), 0D, 1D);
        renderAura(x, y + entity.height / 2, z, scale, tick, Minecraft.getSystemTime(), 0.35F * (tick / (float) EntityPlaceableData.MIN_TIME));
    }

    public void renderAura(double x, double y, double z, double scale, float tick, long systemTime, float alpha) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        //Render cube
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        GlStateManager.pushMatrix();
        GlStateManager.color(0.608F, 0.19F, 0.19F, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
        float i = (systemTime % 5000L) / 5000.0F * 6.0F;
        GlStateManager.translate(i, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        hahaFunnyRenderFunction(x, y, z, scale);
        GlStateManager.popMatrix();
        //Render cube
        GlStateManager.pushMatrix();
        GlStateManager.color(0.808F, 0.39F, 0F, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        float i0 = (systemTime % 6873L) / 6873.0F * 6.0F;
        GlStateManager.translate(-i0, 0.0F, 0.0F);
        GlStateManager.rotate(50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        hahaFunnyRenderFunction(x, y, z, scale);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    public void hahaFunnyRenderFunction(double x, double y, double z, double scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180, 1F, 0, 0);
        GLHelper.BLEND_SRC_ALPHA$ONE.blend();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

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
        GLHelper.BLEND_NORMAL.blend();
        GlStateManager.popMatrix();
    }
}
