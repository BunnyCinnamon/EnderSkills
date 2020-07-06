package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.offence.fire.Explode;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ExplodeRenderer extends SkillRenderer<Explode> {

    private static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/explode.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        RenderMisc.renderEntityOnFire(entity, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            double scale = entity.getRadius() * 2 * MathHelper.clamp(entity.tick / 5D, 0D, 1D);
            if (entity.tick == 10) {
                for (int i = 0; i < 4; i++) {
                    if (entity.world.rand.nextDouble() < 0.8D) {
                        Vec3d vec = entity.getPositionVector();
                        double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                        double posY = vec.y + scale * (entity.world.rand.nextDouble() - 0.5);
                        double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                        float particleScale = 2F + 3F * (float) entity.world.rand.nextGaussian();
                        ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), particleScale, 25, 0xFFE077, ResourceLibrary.GLOW_PARTICLE_EFFECT);
                    }
                }
            }
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            scale = entity.getRadius() * MathHelper.clamp((double) entity.tick / (double) EntityPlaceableData.MIN_TIME, 0D, 1D);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            ShaderLibrary.BRIGHT.begin();
            ShaderLibrary.BRIGHT.set("alpha", 0.7F * (MathHelper.clamp(entity.tick / 10F, 0F, 1F)));
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
            ShaderLibrary.BRIGHT.end();
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return PLACEABLE;
        }
    }
}
