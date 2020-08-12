package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.Hasten;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HastenRenderer extends SkillRenderer<Hasten> {

    public HastenRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 2 == 0 && entity.world.rand.nextDouble() < 0.2D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 1.5F, 18, 0xFFFFFF, 0);
            Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        public static boolean rendering = false;

        @SubscribeEvent
        public void onHandRender(RenderHandEvent event) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
            Capabilities.get(thePlayer).filter(c -> c.isActive(ModAbilities.HASTEN)).ifPresent(c -> {
                GlStateManager.translate((thePlayer.world.rand.nextFloat() - 0.5F) / 20, 0, (thePlayer.world.rand.nextFloat() - 0.5F) / 20);
            });
        }

        @SubscribeEvent
        public void onLivingRender(RenderLivingEvent.Post<EntityLivingBase> event) {
            if (!rendering && !SpeedBoostRenderer.Events.rendering) { //Prevent recursion
                rendering = true; //Lock
                EntityLivingBase entity = event.getEntity();
                Capabilities.get(entity).filter(c -> c.isActive(ModAbilities.HASTEN)).ifPresent(c -> {
                    for (int i = 0; i < 5; ++i) {
                        GlStateManager.pushAttrib();
                        GlStateManager.pushMatrix();
                        GlStateManager.depthMask(false);
                        GlStateManager.alphaFunc(516, 0.003921569f);
                        GlStateManager.enableBlend();
                        GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
                        GlStateManager.color(1F, 1F, 1F, 0.5F);
                        GlStateManager.translate((entity.world.rand.nextFloat() - 0.5F) / 20, 0, (entity.world.rand.nextFloat() - 0.5F) / 20);
                        Render<Entity> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
                        if (render != null) {
                            render.doRender(entity, event.getX(), event.getY(), event.getZ(), entity.rotationYaw, event.getPartialRenderTick());
                        }
                        GlStateManager.disableBlend();
                        GlStateManager.alphaFunc(516, 0.1f);
                        GlStateManager.depthMask(true);
                        GlStateManager.popMatrix();
                        GlStateManager.popAttrib();
                    }
                });
                rendering = false; //Unlock
            }
        }
    }
}
