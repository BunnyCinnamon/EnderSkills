package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.Warp;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class WarpRenderer extends SkillRenderer<Warp> {

    public static final List<WarpRift> WARP_RIFTS = Lists.newArrayList();

    public WarpRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void clientTick(TickEvent.ClientTickEvent event) {
            WARP_RIFTS.removeIf(warpRift -> warpRift.lifeTime++ > warpRift.maxLifeTime || warpRift.reference.get() == null);
        }

        @SubscribeEvent
        public void onRenderAfterWorld(RenderWorldLastEvent event) {
            if (!WARP_RIFTS.isEmpty()) {
                GlStateManager.pushMatrix();
                EntityPlayerSP playerEntity = Minecraft.getMinecraft().player;
                double x = playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * 1F;
                double y = playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * 1F;
                double z = playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * 1F;
                GlStateManager.translate(-x, -y, -z);
                for (WarpRift warpRift : WARP_RIFTS) {
                    warpRift.render(event.getPartialTicks());
                }
                GlStateManager.popMatrix();
            }
        }
    }

    public static class WarpRift {

        public final WeakReference<EntityLivingBase> reference;
        public final Vec3d positionVector;
        public final int maxLifeTime = 20;
        private final boolean inverse;
        public final Random random;
        public final float width;
        public final float height;
        public int lifeTime;

        public WarpRift(EntityLivingBase entity, Vec3d positionVector, boolean inverse) {
            this.reference = new WeakReference<>(entity);
            this.positionVector = positionVector;
            this.inverse = inverse;
            this.random = new Random(entity.world.rand.nextLong());
            this.width = entity.width;
            this.height = entity.height;
        }

        public void render(float partialTicks) {
            EntityLivingBase entity = reference.get();
            if (entity == null) return;
            GlStateManager.pushMatrix();
            GlStateManager.translate(positionVector.x, positionVector.y, positionVector.z);
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.DARK_BACKGROUND);
            } else {
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
            }
            GlStateManager.enableBlend();
            float scale = 1F;
            int growTime = 10;
            int shrinkTime = maxLifeTime - 10;
            if (lifeTime < growTime) {
                scale = (float) lifeTime / (float) growTime;
            } else if (lifeTime > shrinkTime) {
                scale = 1F - (float) (lifeTime - shrinkTime) / 10F;
            }
            double yOffset = inverse ? -0.001D : 0.001D;
            float width = this.width * 1.5F * scale;
            float alpha = 0.4F;
            GL11.glEnable(3042);
            for (int i = 0; i < 4; i++) {
                drawVoid(width, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, true);
                drawVoid(width * 0.9F, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, true);
                drawVoid(width * 0.8F, RenderMisc.getRenderPlayerTime() - i, yOffset, alpha, false);
                yOffset += inverse ? -0.012 : 0.012;
                alpha *= 0.8;
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public void drawVoid(float width, float ticks, double yOffset, float alpha, boolean outline) {
            if (outline) {
                GlStateManager.depthMask(false);
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, outline ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
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
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            if (outline) {
                GlStateManager.depthMask(true);
            }
        }
    }
}
