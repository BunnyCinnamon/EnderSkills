package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Overheal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class OverhealRenderer extends OverlayRenderer<Overheal> {

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public OverhealRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        public final String tickKey = ModEffects.OVERHEAL.getRegistryName() + ":tick";

        @SubscribeEvent()
        public void onLivingPostRender(RenderLivingEvent.Post<EntityLivingBase> event) {
            Render<EntityLivingBase> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(event.getEntity());
            if (!(render instanceof RenderLivingBase)) return;
            if (getTick(event.getEntity(), tickKey) > 0) {
                RenderLivingBase<EntityLivingBase> livingRender = ((RenderLivingBase<EntityLivingBase>) render);
                EntityLivingBase entity = event.getEntity();

                GlStateManager.pushMatrix();
                GlStateManager.translate(event.getX(), event.getY(), event.getZ());
                GlStateManager.enableBlend();
                GlStateManager.depthMask(false);
                GlStateManager.depthFunc(514);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                GlStateManager.pushMatrix();
                GlStateManager.color(0.38F, 0.608F, 0.19F, 1F * (getTick(entity, tickKey) / 10F));
                Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.scale(0.25F, 0.25F, 0.25F);
                float i = (float) (Minecraft.getSystemTime() % 5000L) / 5000.0F * 6.0F;
                GlStateManager.translate(i, 0.0F, 0.0F);
                GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                renderModel(livingRender, entity, event.getPartialRenderTick());
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.color(0.38F, 0.608F, 0.19F, 1F * (getTick(entity, tickKey) / 10F));
                Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                float i0 = (float) (Minecraft.getSystemTime() % 6873L) / 6873.0F * 6.0F;
                GlStateManager.translate(-i0, 0.0F, 0.0F);
                GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                renderModel(livingRender, entity, event.getPartialRenderTick());
                GlStateManager.popMatrix();

                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(515);
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                //Rollback
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }
        }

        @SubscribeEvent
        public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
            if (!event.getEntityLiving().world.isRemote) return;
            boolean active = SkillHelper.isActive(event.getEntity(), ModEffects.OVERHEAL);
            //Handle tick
            if (active) {
                if (getTick(event.getEntity(), tickKey) < 10) {
                    setTick(event.getEntity(), tickKey, getTick(event.getEntity(), tickKey) + 1);
                }
            } else {
                if (getTick(event.getEntity(), tickKey) > 0) {
                    setTick(event.getEntity(), tickKey, getTick(event.getEntity(), tickKey) - 1);
                }
            }
        }
    }
}
