package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.defense.light.Charm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

@SideOnly(Side.CLIENT)
public class CharmRenderer extends SkillRenderer<Charm> {

    private static final ResourceLocation SHADER = new ResourceLocation(LibMod.MOD_ID, "shaders/post/charm.json");

    public CharmRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
        EntityThrowableDataRenderer.add(ModAbilities.CHARM, ProjectileLightRenderer::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 10 == 0 && entity.world.rand.nextDouble() < 0.02D) {
            Vec3d vec = entity.getPositionEyes(1F);
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() - 0.5D;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.01, 0), 2F, 50, 0xFFFFFF, ResourceLibrary.ANGRY);
        }
        Optional.ofNullable(SkillHelper.getOwner(skillHolder.data)).ifPresent(owner -> {
            Vector from = RenderMisc.getRenderViewVector(entity, partialTicks);
            Vector to = RenderMisc.getRenderViewVector(owner, partialTicks).subtract(from);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineWidth(2F);
            GL11.glLineStipple(1, (short)0xFF);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(0, entity.height / 2, 0).color(1F, 0F, 0F, 0.5F).endVertex();
            buffer.pos(to.x, to.y + owner.getEyeHeight() - 0.1, to.z).color(1F, 0F, 0F, 0.5F).endVertex();
            tessellator.draw();
            GL11.glLineStipple(1, (short)0xFFFF);
            GL11.glLineWidth(1F);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        });
    }

    @SideOnly(Side.CLIENT)
    public static class Events {
        public boolean wasActive = false;

        @SubscribeEvent
        @SuppressWarnings("ConstantConditions")
        public void playerTick(TickEvent.ClientTickEvent event) {
            if (event.type == TickEvent.Type.CLIENT) {
                EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
                if (SkillHelper.isActive(Minecraft.getMinecraft().player, ModAbilities.CHARM)) {
                    if (!wasActive) {
                        renderer.loadShader(SHADER);
                        wasActive = true;
                    }
                } else if (wasActive) {
                    if (renderer.getShaderGroup() != null && renderer.getShaderGroup().getShaderGroupName() != null && SHADER.toString().equals(renderer.getShaderGroup().getShaderGroupName())) {
                        renderer.stopUseShader();
                    }
                    wasActive = false;
                }
            }
        }
    }
}
