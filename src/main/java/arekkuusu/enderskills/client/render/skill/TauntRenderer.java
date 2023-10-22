package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Taunt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Optional;

@SideOnly(Side.CLIENT)
public class TauntRenderer extends SkillRenderer<Taunt> {

    private static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/taunt.png");
    private static final ResourceLocation SHADER = new ResourceLocation(LibMod.MOD_ID, "shaders/post/taunt.json");

    public TauntRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.TAUNT, Placeable::new);
        MinecraftForge.EVENT_BUS.register(new Events());
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
            GL11.glLineWidth(2);
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
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            int tick = Math.min(entity.tick, EntityPlaceableData.MIN_TIME);
            double scale = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", (1F - (float) entity.tick / (float) entity.getLifeTime()));
            }
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            this.bindTexture(getEntityTexture(entity));
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

            buffer.pos(scale, scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, scale, -scale).tex(1, 0).endVertex();

            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 1).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 1).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, -scale).tex(0, 0).endVertex();
            buffer.pos(-scale, -scale, scale).tex(0, 1).endVertex();
            buffer.pos(scale, -scale, scale).tex(1, 1).endVertex();
            buffer.pos(scale, -scale, -scale).tex(1, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return PLACEABLE;
        }
    }

    public static class Events {
        public boolean wasActive;

        @SubscribeEvent
        @SuppressWarnings("ConstantConditions")
        public void playerTick(TickEvent.ClientTickEvent event) {
            if (event.type == TickEvent.Type.CLIENT) {
                EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
                if (SkillHelper.isActive(Minecraft.getMinecraft().player, ModAbilities.TAUNT)) {
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
