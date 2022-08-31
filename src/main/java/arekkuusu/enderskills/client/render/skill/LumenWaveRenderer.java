package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableLumenWave;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.offence.light.LumenWave;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class LumenWaveRenderer extends SkillRenderer<LumenWave> {

    public static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/lumen_wave.png");

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableLumenWave> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableLumenWave entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            if (entity.tick > entity.getLifeTime()) return;
            if (entity.tick % 5 == 0) {
                for (int i = 0; i < 6; i++) {
                    if (entity.world.rand.nextDouble() < 0.05D && ClientProxy.canParticleSpawn()) {
                        Vec3d vec = entity.getPositionVector();
                        double posX = vec.x + entity.width * (entity.world.rand.nextDouble() - 0.5);
                        double posY = vec.y + entity.height * (entity.world.rand.nextDouble() - 0.5);
                        double posZ = vec.z + entity.width * (entity.world.rand.nextDouble() - 0.5);
                        EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 1, 50, 0xFFFFFF, ResourceLibrary.MINUS);
                    }
                }
            }
            double progress = ((double) entity.tick + partialTicks) / (double) entity.getLifeTime();
            double scale = (entity.getRadius() + 3) * 2 * progress;
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            Vector vector = new Vector(entity.motionX, entity.motionY, entity.motionZ).normalize();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 0.6F * (1F - (float) progress));
            }
            for (int i = -1; i <= 1; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y + entity.getEyeHeight() / 2, z);
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
                ShaderLibrary.ALPHA.end();
            }
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 0.5F * (1F - (float) progress));
            }
            for (int i = -1; i <= 1; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y + entity.getEyeHeight() / 2, z);
                GlStateManager.translate(-vector.x, -vector.y, -vector.z);
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
                ShaderLibrary.ALPHA.end();
            }
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 0.4F * (1F - (float) progress));
            }
            for (int i = -1; i <= 1; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y + entity.getEyeHeight() / 2, z);
                GlStateManager.translate(-vector.x * 2D, -vector.y * 2D, -vector.z * 2D);
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
                ShaderLibrary.ALPHA.end();
            }
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 0.3F * (1F - (float) progress));
            }
            for (int i = -1; i <= 1; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y + entity.getEyeHeight() / 2, z);
                GlStateManager.translate(-vector.x * 3D, -vector.y * 3D, -vector.z * 3D);
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
                ShaderLibrary.ALPHA.end();
            }
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableLumenWave entity) {
            return PLACEABLE;
        }
    }
}
