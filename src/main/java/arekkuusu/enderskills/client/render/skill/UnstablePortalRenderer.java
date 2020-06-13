package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.UnstablePortal;
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
public class UnstablePortalRenderer extends SkillRenderer<UnstablePortal> {

    public UnstablePortalRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.UNSTABLE_PORTAL, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.UNSTABLE_PORTAL, ProjectileVoid::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        public final CoreGLE gle;

        public Placeable(RenderManager renderManager) {
            super(renderManager);
            this.gle = new CoreGLE();
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            float scale = 1F;
            int growTime = EntityPlaceableData.MIN_TIME;
            int shrinkTime = entity.getLifeTime() - 10;
            if (entity.tick < growTime) {
                scale = (float) entity.tick / (float) growTime;
            } else if (entity.tick > shrinkTime) {
                scale = 1F - (float) (entity.tick - shrinkTime) / 10F;
            }
            this.bindTexture(getEntityTexture(entity));
            if (!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if (!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 0.9F, 0.12F, 0.4F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime() * 75F);
                    ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime() * 75F);
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
                }
            }
            GL11.glEnable(3042);
            float stab = 1F - 15 / 50F;
            float ticks = RenderMisc.getRenderPlayerTime() * 6F;
            if (entity.tick > growTime && entity.tick < shrinkTime) {
                scale += Math.sin(ticks / 8F) * 0.10000000149011612F * stab;
            }
            for (int q = 0; q <= 3; ++q) {
                GL11.glPushMatrix();
                float s = entity.getRadius() * ((q < 3) ? (1.1F + 0.05F * q) : 1F) * scale;
                GlStateManager.scale(s, s, s);
                if (q < 3) {
                    GlStateManager.depthMask(false);
                }
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderMisc.drawCubeRaw();
                if (q < 3) {
                    GlStateManager.depthMask(true);
                }
                GL11.glPopMatrix();
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
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            if (!CommonConfig.RENDER_CONFIG.rendering.vanilla || CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                return ResourceLibrary.DARK_BACKGROUND;
            } else {
                return ResourceLibrary.PORTAL_BACKGROUND;
            }
        }
    }
}
