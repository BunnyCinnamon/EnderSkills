package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.Hover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class HoverRenderer extends SkillRenderer<Hover> {

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 2 == 0 && entity.world.rand.nextDouble() < 0.4D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + 0.1D * (entity.world.rand.nextDouble() - 0.5D);
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            entity.world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY, posZ, 0, 0.1, 0);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (!CommonConfig.RENDER_CONFIG.rendering.vanilla || CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            this.bindTexture(ResourceLibrary.DARK_BACKGROUND);
        } else {
            this.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
        }
        GlStateManager.enableBlend();
        double yOffset = entity.onGround ? 0.001D : -0.001D;
        float width = entity.width * 1.5F;
        float alpha = 0.4F;
        GL11.glEnable(3042);
        for (int i = 0; i < 4; i++) {
            drawVoid(width, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, true);
            drawVoid(width * 0.9F, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, true);
            drawVoid(width *  0.8F, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, false);
            yOffset += 0.012;
            alpha *= 0.8;
        }
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(3042);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void drawVoid(float width, float ticks, double yOffset, float alpha, boolean outline) {
        if(outline) {
            GlStateManager.depthMask(false);
        }
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, outline ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
        if(!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            if(!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                ShaderLibrary.UNIVERSE.begin();
                ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                ShaderLibrary.UNIVERSE.set("ticks", ticks);
                ShaderLibrary.UNIVERSE.set("alpha", alpha);
            } else {
                ShaderLibrary.UNIVERSE_DEFAULT.begin();
                ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                ShaderLibrary.UNIVERSE_DEFAULT.set("time", ticks);
                ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", alpha);
            }
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, yOffset, 0);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        if(!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            if(!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                ShaderLibrary.UNIVERSE.end();
            } else {
                ShaderLibrary.UNIVERSE_DEFAULT.end();
            }
        }
        if(outline) {
            GlStateManager.depthMask(true);
        }
    }
}
