package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.light.RadiantRay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RadiantRayRenderer extends SkillRenderer<RadiantRay> {

    private static final ResourceLocation GLINT = new ResourceLocation(LibMod.MOD_ID, "textures/entity/shocking_aura2.png");

    public RadiantRayRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.RADIANT_RAY, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.RADIANT_RAY, ProjectileLightRenderer::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            if(entity.tick > entity.getLifeTime()) return;
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
            double mod = 0;
            double scale = entity.getRadius() * 2;
            if (entity.tick + partialTicks < 4) {
                mod = 1 * MathHelper.clamp((double) (entity.tick + partialTicks) / 3, 0D, 1D);
            } else if (entity.tick + partialTicks > entity.getLifeTime() - 4) {
                mod = 1 * MathHelper.clamp((double) (entity.getLifeTime() - ((entity.tick + partialTicks) - 3)) / 3, 0D, 1D);
            } else {
                mod = 1;
            }
            scale *= mod;
            long totalWorldTime = (long) (entity.world.getTotalWorldTime() + partialTicks);
            GlStateManager.color(1F, 1F, 1F, 1F);
            //Render cube
            GlStateManager.pushMatrix();
            float lastBrightnessX = OpenGlHelper.lastBrightnessX;
            float lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GLHelper.lightMap(200,200);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            GlStateManager.pushMatrix();
            GlStateManager.color(1,1,1, (float) (0.3 * mod));
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.scale(0.06F, 0.06F, 0.06F);
            GlStateManager.translate((Minecraft.getSystemTime() % 5000L) / 5000.0F * 6.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            x = x - 0.5;
            z = z - 0.5;
            renderBeamSegment(x, y, z, partialTicks, 1, totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale * 0.5F, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, -totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale * 0.5F, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, -totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale, scale);

            GlStateManager.popMatrix();
            //Render cube
            GlStateManager.pushMatrix();
            GlStateManager.color(1,1,1, (float) (0.3 * mod));
            Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.scale(0.1F, 0.1F, 0.1F);
            float i0 = (Minecraft.getSystemTime() % 6873L) / 6873.0F * 6.0F;
            GlStateManager.translate(-i0, 0.0F, 0.0F);
            GlStateManager.rotate(50.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            renderBeamSegment(x, y, z, partialTicks, 1, totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale * 0.5F, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, -totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale * 0.5F, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale, scale);
            renderBeamSegment(x, y, z, partialTicks, 1, -totalWorldTime, (int) -entity.getRadius(), (int) (256 - entity.posY), scale, scale);

            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GLHelper.lightMap(lastBrightnessX,lastBrightnessY);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        public void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale, double totalWorldTime, int yOffset, int height, double beamRadius, double glowRadius) {
            int i = yOffset + height;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            double d0 = totalWorldTime + partialTicks;
            double d1 = height < 0 ? d0 : -d0;
            double d2 = MathHelper.frac(d1 * 0.2D - (double)MathHelper.floor(d1 * 0.1D));
            double d3 = d0 * 0.025D * -1.5D;
            double d4 = 0.5D + Math.cos(d3 + 2.356194490192345D) * beamRadius;
            double d5 = 0.5D + Math.sin(d3 + 2.356194490192345D) * beamRadius;
            double d6 = 0.5D + Math.cos(d3 + (Math.PI / 4D)) * beamRadius;
            double d7 = 0.5D + Math.sin(d3 + (Math.PI / 4D)) * beamRadius;
            double d8 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * beamRadius;
            double d9 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * beamRadius;
            double d10 = 0.5D + Math.cos(d3 + 5.497787143782138D) * beamRadius;
            double d11 = 0.5D + Math.sin(d3 + 5.497787143782138D) * beamRadius;
            double d14 = -1.0D + d2;
            double d15 = (double)height * textureScale * (0.5D / beamRadius) + d14;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(x + d4, y + (double)i, z + d5).tex(1.0D, d15).endVertex();
            bufferbuilder.pos(x + d4, y + (double)yOffset, z + d5).tex(1.0D, d14).endVertex();
            bufferbuilder.pos(x + d6, y + (double)yOffset, z + d7).tex(0.0D, d14).endVertex();
            bufferbuilder.pos(x + d6, y + (double)i, z + d7).tex(0.0D, d15).endVertex();
            bufferbuilder.pos(x + d10, y + (double)i, z + d11).tex(1.0D, d15).endVertex();
            bufferbuilder.pos(x + d10, y + (double)yOffset, z + d11).tex(1.0D, d14).endVertex();
            bufferbuilder.pos(x + d8, y + (double)yOffset, z + d9).tex(0.0D, d14).endVertex();
            bufferbuilder.pos(x + d8, y + (double)i, z + d9).tex(0.0D, d15).endVertex();
            bufferbuilder.pos(x + d6, y + (double)i, z + d7).tex(1.0D, d15).endVertex();
            bufferbuilder.pos(x + d6, y + (double)yOffset, z + d7).tex(1.0D, d14).endVertex();
            bufferbuilder.pos(x + d10, y + (double)yOffset, z + d11).tex(0.0D, d14).endVertex();
            bufferbuilder.pos(x + d10, y + (double)i, z + d11).tex(0.0D, d15).endVertex();
            bufferbuilder.pos(x + d8, y + (double)i, z + d9).tex(1.0D, d15).endVertex();
            bufferbuilder.pos(x + d8, y + (double)yOffset, z + d9).tex(1.0D, d14).endVertex();
            bufferbuilder.pos(x + d4, y + (double)yOffset, z + d5).tex(0.0D, d14).endVertex();
            bufferbuilder.pos(x + d4, y + (double)i, z + d5).tex(0.0D, d15).endVertex();
            tessellator.draw();
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
