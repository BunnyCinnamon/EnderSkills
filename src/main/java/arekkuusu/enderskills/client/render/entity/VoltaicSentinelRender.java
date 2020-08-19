package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.client.render.model.ModelSentinel;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.SpriteLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.sprite.UVFrame;
import arekkuusu.enderskills.common.entity.EntityVoltaicSentinel;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class VoltaicSentinelRender extends RenderLiving<EntityVoltaicSentinel> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/voltaic_sentinel.png");
    public static final ResourceLocation TEXTURE_ = new ResourceLocation(LibMod.MOD_ID, "textures/entity/voltaic_sentinel_.png");

    public VoltaicSentinelRender(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSentinel(), 0.5F);
    }

    @Override
    public void doRender(EntityVoltaicSentinel entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<>(entity, this, partialTicks, x, y, z)))
            return;
        //Render effects
        GlStateManager.pushMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        ShaderLibrary.ALPHA.begin();
        float bright = MathHelper.cos(RenderMisc.getRenderPlayerTime() * 0.05F) * 0.8F;
        if (bright < 0) bright *= -1;
        ShaderLibrary.ALPHA.set("alpha", 0.1F + bright);
        GlStateManager.translate(x, y, z);
        renderMirror(entity.ticksExisted + partialTicks, 0.75F, 0.5F);
        renderMirror(-entity.ticksExisted - partialTicks, 0.5F, 0.75F);
        renderMirror(entity.ticksExisted + partialTicks, -0.3F, 1F);
        ShaderLibrary.ALPHA.end();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.depthMask(false);
        GlStateManager.translate(x, y, z);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        int color = entity.getAggro() ? 0xFFA8A8 : 0xFFECA8;
        ShaderLibrary.ALPHA.begin();
        ShaderLibrary.ALPHA.set("alpha", 0.5F + bright);
        RenderMisc.renderBeams(RenderMisc.getRenderPlayerTime() * 0.005F, 35, color, color, entity.getAggro() ? 0.8F : 0.7F);
        ShaderLibrary.ALPHA.end();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        //Render model
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        this.mainModel.isRiding = shouldSit;
        this.mainModel.isChild = entity.isChild();

        try {
            float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
                EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
                f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapDegrees(f2);

                if (f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F) {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F) {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            this.renderLivingAt(entity, x, y, z);
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.applyRotations(entity, f8, f, partialTicks);
            float f4 = this.prepareScale(entity, partialTicks);
            float f5 = 0.0F;
            float f6 = 0.0F;

            if (!entity.isRiding()) {
                f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                if (entity.isChild()) {
                    f6 *= 3.0F;
                }

                if (f5 > 1.0F) {
                    f5 = 1.0F;
                }
                f2 = f1 - f; // Forge: Fix MC-1207
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);

            if (this.renderOutlines) {
                boolean flag1 = this.setScoreTeamColor(entity);
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(this.getTeamColor(entity));

                if (!this.renderMarker) {
                    this.renderModel(entity, f6, f5, f8, f2, f7, f4);
                }

                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();

                if (flag1) {
                    this.unsetScoreTeamColor();
                }
            } else {
                boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                this.renderModel(entity, f6, f5, f8, f2, f7, f4);

                if (flag) {
                    this.unsetBrightness();
                }

                GlStateManager.depthMask(true);
            }

            GlStateManager.disableRescaleNormal();
        } catch (Exception ignored) {
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<>(entity, this, partialTicks, x, y, z));
    }

    public static void renderMirror(float tick, float rotationOffset, float scale) {
        GlStateManager.pushMatrix();
        SpriteLibrary.VOLTAIC_SENTINEL.bind();
        UVFrame frame = SpriteLibrary.VOLTAIC_SENTINEL.getFrame(tick * 0.25F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buff = tessellator.getBuffer();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(tick * rotationOffset % 360F, 0F, 1F, 0F);
        GlStateManager.rotate(tick * rotationOffset % 360F, 1F, 0F, 0F);
        GlStateManager.rotate(tick * rotationOffset % 360F, 0F, 0F, 1F);
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buff.pos(-0.5, -0.5, 0).tex(frame.uMin, frame.vMax).endVertex();
        buff.pos(0.5, -0.5, 0).tex(frame.uMax, frame.vMax).endVertex();
        buff.pos(0.5, 0.5, 0).tex(frame.uMax, frame.vMin).endVertex();
        buff.pos(-0.5, 0.5, 0).tex(frame.uMin, frame.vMin).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    protected ResourceLocation getEntityTexture(EntityVoltaicSentinel entity) {
        return TEXTURE_;
    }
}