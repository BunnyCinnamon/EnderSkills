package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.entity.EntitySolarLance;
import arekkuusu.enderskills.common.skill.ability.offence.ender.BlackHole;
import arekkuusu.enderskills.common.skill.ability.offence.light.SolarLance;
import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class SolarLanceRenderer extends SkillRenderer<SolarLance> {

    public static class Placeable extends Render<EntitySolarLance> {

        public final CoreGLE gle;

        public Placeable(RenderManager renderManager) {
            super(renderManager);
            this.gle = new CoreGLE();
        }

        @Override
        public void doRender(EntitySolarLance entity, double x, double y, double z, float entityYaw, float partialTicks) {
            float scale = (float) entity.getScale(partialTicks);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-entity.rotationYaw - 90, 0F, 1F, 0F);
            GlStateManager.rotate(entity.rotationPitch, 0F, 0F, 1F);
            this.bindTexture(getEntityTexture(entity));
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 122F / 255F, 1.0f,  252F / 255F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime() * 5);
                    ShaderLibrary.UNIVERSE.set("alpha", 1F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.set("time", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.set("alpha", 1F);
                }
            }
            GL11.glEnable(3042);
            GlStateManager.depthMask(false);
            for (int q = 0; q <= 3; ++q) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
                if (entity.points.size() > 2) {
                    GlStateManager.pushMatrix();
                    double[][] pp = new double[entity.points.size()][3];
                    float[][] colours = new float[entity.points.size()][4];
                    double[] radii = new double[entity.points.size()];
                    for (int a = 0; a < entity.points.size(); ++a) {
                        pp[a][0] = entity.points.get(a).x;
                        pp[a][1] = entity.points.get(a).y;
                        pp[a][2] = entity.points.get(a).z;
                        colours[a][0] = 1F;
                        colours[a][1] = 1F;
                        colours[a][2] = 1F;
                        colours[a][3] = 1F;
                        radii[a] = entity.pointsWidth.get(a) * ((q < 3) ? (1.05F + 0.025F * q) : 1F) * scale;
                    }
                    this.gle.set_POLYCYL_TESS(12);
                    this.gle.gleSetJoinStyle(1026);
                    this.gle.glePolyCone(pp.length, pp, colours, radii, 1F, 0F);
                    GlStateManager.popMatrix();
                }
            }
            this.bindTexture(ResourceLibrary.WHITE_BACKGROUND);
            GlStateManager.depthMask(true);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT_WHITE.end();
                }
            }
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntitySolarLance EntityPortal) {
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                return ResourceLibrary.WHITE_BACKGROUND;
            } else {
                return ResourceLibrary.PORTAL_BACKGROUND;
            }
        }
    }
}
