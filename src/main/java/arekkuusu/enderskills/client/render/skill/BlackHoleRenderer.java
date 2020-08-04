package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.common.entity.EntityBlackHole;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.ender.BlackHole;
import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class BlackHoleRenderer extends SkillRenderer<BlackHole> {

    public BlackHoleRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.BLACK_HOLE, ProjectileVoid::new);
        MinecraftForge.EVENT_BUS.register(new FogRenderer.Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {
        @SubscribeEvent
        public void onSoundEffect(PlaySoundAtEntityEvent event) {
            if (event.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) event.getEntity();
                Capabilities.get(entity).flatMap(c -> c.getActive(ModAbilities.BLACK_HOLE)).ifPresent(holder -> {
                    event.setVolume(event.getVolume() * 0.5F);
                });
            }
        }

        @SubscribeEvent
        public void onFogDensityRender(EntityViewRenderEvent.FogDensity event) {
            SkillHelper.getActive(event.getEntity(), ModAbilities.BLACK_HOLE, holder -> {
                float f1 = 0F;
                int i = holder.data.time - holder.tick;
                if (i < 20) {
                    f1 = f1 + (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 - f1) * (1.0F - (float) i / 20.0F);
                }
                GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
                GlStateManager.setFogStart(f1 * 0.25F);
                GlStateManager.setFogEnd(f1);
                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GlStateManager.glFogi(34138, 34139);
                }
                event.setDensity(1F);
                event.setCanceled(true);
            });
        }
    }

    public static class Placeable extends Render<EntityBlackHole> {

        public final CoreGLE gle;

        public Placeable(RenderManager renderManager) {
            super(renderManager);
            this.gle = new CoreGLE();
        }

        @Override
        public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks) {
        /*GlStateManager.pushMatrix();

        Framebuffer framebuffer = new Framebuffer(200, 200, false);
        framebuffer.bindFramebuffer(false);

        //Render to FrameBuffer
        GlStateManager.pushAttrib();*/
            float scale = (float) entity.getScale(entity.tick);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            this.bindTexture(getEntityTexture(entity));
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
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
                    GL11.glPushMatrix();
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
                    GL11.glPopMatrix();
                }
            }
            this.bindTexture(ResourceLibrary.DARK_BACKGROUND);
            GlStateManager.depthMask(true);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            scale *= entity.getRadius() * 1.5F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            RenderMisc.drawObj(0xFFFFFF, 1F, RenderMisc.sphereId);
            GL11.glPopMatrix();
        /*GlStateManager.popMatrix();
        GlStateManager.popAttrib();
        framebuffer.unbindFramebuffer();
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);

        //Render Frame Buffer texture
        framebuffer.bindFramebufferTexture();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        *//*GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);*//*
        GlStateManager.translate(x, y, z);
        *//*GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);*//*
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-1D, -1D, 0).tex(1, 0).endVertex();
        buffer.pos(1D, -1D, 0).tex(0, 0).endVertex();
        buffer.pos(1D, 1D, 0).tex(0, 1).endVertex();
        buffer.pos(-1D, 1D, 0).tex(1, 1).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        framebuffer.unbindFramebufferTexture();

        GlStateManager.popMatrix();*/
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

        private void setColour(EntityBlackHole entity, double age, double alpha) {
            double life = (age / entity.getLifeTime());
            double f = Math.max(0, (life - EntityBlackHole.collapse) / (1 - EntityBlackHole.collapse));
            f = Math.max(f, 1 - (life * 30));
            GlStateManager.color((float) f, (float) f, (float) f, (float) alpha);
        }
    }
}
