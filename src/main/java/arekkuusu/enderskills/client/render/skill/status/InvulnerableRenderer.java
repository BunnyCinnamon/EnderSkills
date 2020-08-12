package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Invulnerable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class InvulnerableRenderer extends SkillRenderer<Invulnerable> {

    private static final ResourceLocation FOLLOWING_HEAD = new ResourceLocation(LibMod.MOD_ID, "textures/entity/nearby_invincibility_0.png");

    public InvulnerableRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent()
        public void onLivingPreRender(RenderLivingEvent.Post<EntityLivingBase> event) {
            if (getTick(event.getEntity()) > 0) {
                Entity entity = event.getEntity();
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.pushMatrix();
                GlStateManager.translate(event.getX(), event.getY() + entity.height / 2, event.getZ());
                GlStateManager.rotate(180, 1F, 0, 0);
                GlStateManager.rotate(entity.ticksExisted * 0.75F % 360F, 0F, 1F, 0F);
                GLHelper.BLEND_SRC_ALPHA$ONE.blend();
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BRIGHT.begin();
                    ShaderLibrary.BRIGHT.set("alpha", 0.5F * (float) getTick(entity) / 10F);
                }
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                this.bindTexture(FOLLOWING_HEAD);
                drawHeadWithOffset(0.2, -(entity.height / 2) - 0.1);
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BRIGHT.end();
                }
                GLHelper.BLEND_NORMAL.blend();
                GlStateManager.popMatrix();
            }
        }

        @SubscribeEvent
        public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
            if(!event.getEntityLiving().world.isRemote) return;
            boolean active = SkillHelper.isActive(event.getEntity(), ModEffects.INVULNERABLE);
            //Handle tick
            if (active) {
                if (getTick(event.getEntity()) < 10) {
                    setTick(event.getEntity(), getTick(event.getEntity()) + 1);
                }
            } else {
                if (getTick(event.getEntity()) > 0) {
                    setTick(event.getEntity(), getTick(event.getEntity()) - 1);
                }
            }
        }

        public final String key = ModEffects.INVULNERABLE.getRegistryName() + ":tick";

        public int getTick(Entity entity) {
            NBTTagCompound nbt = entity.getEntityData();
            return nbt.hasKey(key) ? nbt.getInteger(key) : -1;
        }

        public void setTick(Entity entity, int tick) {
            NBTTagCompound nbt = entity.getEntityData();
            nbt.setInteger(key, tick);
        }

        public void bindTexture(ResourceLocation location) {
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(location);
        }

        public void drawHeadWithOffset(double scale, double offset) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, offset, 0);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();

            buffer.pos(scale, -scale, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(-scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(0, 0).endVertex();

            buffer.pos(-scale, -scale, scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 0).endVertex();

            buffer.pos(scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, -scale).tex(0, 0).endVertex();

            tessellator.draw();
            GlStateManager.popMatrix();
        }
    }
}
