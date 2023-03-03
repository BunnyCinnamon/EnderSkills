package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.SpriteLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.sprite.UVFrame;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.electric.MagneticPull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MagneticPullRenderer extends SkillRenderer<MagneticPull> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/magnetic_pull.png");

    public MagneticPullRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.MAGNETIC_PULL, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.MAGNETIC_PULL, ProjectileElectricRenderer::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.depthMask(false);
            GlStateManager.translate(x, y + 0.1, z);
            GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90F + ((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX), 1.0F, 0.0F, 0.0F);
            double scale = NBTHelper.getDouble(entity.getData().nbt, "range") * MathHelper.clamp(((float) entity.tick / 10F), 0F, 1F);

            GlStateManager.rotate(180, 1F, 0, 0);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", SkillRenderer.getDiffuseBlend(entity.tick, entity.getLifeTime(), 0.8F));
            }
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            renderRing(scale, entity.tick);
            renderFence(entity, 0F, 0F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 1, scale);
            renderFence(entity, 0F, 0.5F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 2.5F, 4, scale * 0.5F);
            renderFence(entity, 0F, 1F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 3, scale * 0.2F);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }

        public void renderRing(double scale, int tick) {
            SpriteLibrary.MAGNETIC_PULL.bind();
            UVFrame frame = SpriteLibrary.MAGNETIC_PULL.getFrame(tick);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(scale, 0, -scale).tex(frame.uMax, frame.vMin).endVertex();
            buffer.pos(scale, 0, scale).tex(frame.uMax, frame.vMax).endVertex();
            buffer.pos(-scale, 0, scale).tex(frame.uMin, frame.vMax).endVertex();
            buffer.pos(-scale, 0, -scale).tex(frame.uMin, frame.vMin).endVertex();

            buffer.pos(-scale, 0, -scale).tex(frame.uMin, frame.vMin).endVertex();
            buffer.pos(-scale, 0, scale).tex(frame.uMin, frame.vMax).endVertex();
            buffer.pos(scale, 0, scale).tex(frame.uMax, frame.vMax).endVertex();
            buffer.pos(scale, 0, -scale).tex(frame.uMax, frame.vMin).endVertex();
            tessellator.draw();
        }

        public void renderFence(Entity entity, float x, float y, float z, float partialTicks, float angleOffset, int textureOffset, double scale) {
            GlStateManager.pushMatrix();
            SpriteLibrary.ELECTRIC_RING.bind();
            for (int i = 0; i < 4; i++) {
                UVFrame frame = SpriteLibrary.ELECTRIC_RING.getFrame(entity.ticksExisted + textureOffset + partialTicks + i);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(((360F / 4F) * i) + (entity.ticksExisted * angleOffset + partialTicks) % 360, 0F, 1F, 0F);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-scale, -scale, -scale).tex(frame.uMin, frame.vMin).endVertex();
                buffer.pos(scale, -scale, -scale).tex(frame.uMax, frame.vMin).endVertex();
                buffer.pos(scale, scale, -scale).tex(frame.uMax, frame.vMax).endVertex();
                buffer.pos(-scale, scale, -scale).tex(frame.uMin, frame.vMax).endVertex();
                buffer.pos(-scale, scale, -scale).tex(frame.uMin, frame.vMax).endVertex();
                buffer.pos(scale, scale, -scale).tex(frame.uMax, frame.vMax).endVertex();
                buffer.pos(scale, -scale, -scale).tex(frame.uMax, frame.vMin).endVertex();
                buffer.pos(-scale, -scale, -scale).tex(frame.uMin, frame.vMin).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
