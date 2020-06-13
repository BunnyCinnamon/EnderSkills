package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.EntityPortal;
import arekkuusu.enderskills.common.lib.LibMod;
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
public class EntityPortalRender extends Render<EntityPortal> {

    public final CoreGLE gle;

    public EntityPortalRender(RenderManager renderManager) {
        super(renderManager);
        this.gle = new CoreGLE();
    }

    @Override
    public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GL11.glPushMatrix();
        GlStateManager.translate(x, y, z);
        this.bindTexture(getEntityTexture(entity));
        if (!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            if (!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                ShaderLibrary.UNIVERSE.begin();
                ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
            } else {
                ShaderLibrary.UNIVERSE_DEFAULT.begin();
                ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
            }
        }
        float amp = 1.0F;
        float stab = 1F - 4 / 50F;
        float wm = entity.isOpen()
                ? (entity.openAnimationTimer > 0 ? 1F - 0.9F * (entity.openAnimationTimer - partialTicks) / 10F : 1F) //Open width animation
                : 0.1F; //Default closed width
        GL11.glEnable(3042);
        for (int q = 0; q <= 3; ++q) {
            if (q < 3) {
                GlStateManager.depthMask(false);
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (entity.points.size() > 2) {
                GL11.glPushMatrix();
                double[][] pp = new double[entity.points.size()][3];
                float[][] colours = new float[entity.points.size()][4];
                double[] radii = new double[entity.points.size()];
                for (int a = 0; a < entity.points.size(); ++a) {
                    float tick = entity.ticksExisted + partialTicks + q;
                    if (a > entity.points.size() / 2) {
                        tick -= a * 10;
                    } else if (a < entity.points.size() / 2) {
                        tick += a * 10;
                    }
                    pp[a][0] = entity.points.get(a).x;
                    pp[a][1] = entity.points.get(a).y;
                    pp[a][2] = entity.points.get(a).z;
                    colours[a][0] = 1F;
                    colours[a][1] = 1F;
                    colours[a][2] = 1F;
                    colours[a][3] = 1F;
                    final double w = 1.0 - Math.sin(tick / 8F * amp) * 0.10000000149011612F * stab;
                    radii[a] = entity.pointsWidth.get(a) * w * ((q < 3) ? (1.1F + 0.15F * q) : 1F) * wm;
                }
                this.gle.set_POLYCYL_TESS(6);
                this.gle.gleSetJoinStyle(1026);
                this.gle.glePolyCone(pp.length, pp, colours, radii, 1F, 0F);
                GL11.glPopMatrix();
            }
            if (q < 3) {
                GlStateManager.depthMask(true);
            }
        }
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(3042);
        if (!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            if (!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                ShaderLibrary.UNIVERSE.end();
            } else {
                ShaderLibrary.UNIVERSE_DEFAULT.end();
            }
        }
        GL11.glPopMatrix();
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(EntityPortal EntityPortal) {
        if (!CommonConfig.RENDER_CONFIG.rendering.vanilla || CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
            return ResourceLibrary.DARK_BACKGROUND;
        } else {
            return ResourceLibrary.PORTAL_BACKGROUND;
        }
    }
}
