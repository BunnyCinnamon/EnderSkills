package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.entity.EntityBlackHole;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.ender.BlackHole;
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
public class BlackHoleRenderer extends SkillRenderer<BlackHole> {

    public BlackHoleRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.BLACK_HOLE, ProjectileVoid::new);
    }

    public static class Placeable extends Render<EntityBlackHole> {

        public final CoreGLE gle;

        public Placeable(RenderManager renderManager) {
            super(renderManager);
            this.gle = new CoreGLE();
        }

        @Override
        public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks) {
            float scale = (float) entity.getScale(entity.tick);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            this.bindTexture(getEntityTexture(entity));
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE.set("alpha", 1F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 1F);
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
            this.bindTexture(ResourceLibrary.DARK_BACKGROUND);
            GlStateManager.depthMask(true);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            scale *= entity.getRadius() * 1.5F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            RenderMisc.drawObj(0xFFFFFF, 1F, RenderMisc::drawSphereRaw);
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityBlackHole EntityPortal) {
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                return ResourceLibrary.DARK_BACKGROUND;
            } else {
                return ResourceLibrary.PORTAL_BACKGROUND;
            }
        }
    }
}
