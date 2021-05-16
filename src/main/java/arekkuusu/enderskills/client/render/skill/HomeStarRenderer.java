package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.SpriteLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.sprite.UVFrame;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.defense.electric.MagneticPull;
import arekkuusu.enderskills.common.skill.ability.defense.fire.HomeStar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class HomeStarRenderer extends SkillRenderer<HomeStar> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/fire_spirit.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.translate(x, y + 0.1, z);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        double angle = (this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX;
        double progress = MathHelper.clamp((double) skillHolder.tick / (double) Math.min(skillHolder.data.time, EntityPlaceableData.MIN_TIME), 0D, 1D);
        double scale = NBTHelper.getDouble(skillHolder.data.nbt, "range") * progress;

        if (entity == Minecraft.getMinecraft().getRenderViewEntity()) {
            if (entity.onGround && angle > 0) angle = 0;
            else if (angle < 0) angle = Math.max(-20D, angle);
            else if (angle > 0) angle = Math.min(20D, angle);
        }

        if (entity != Minecraft.getMinecraft().getRenderViewEntity() || (!entity.onGround || angle < 0)) {
            if (entity.onGround) angle = Math.max(-20D, angle);
            GlStateManager.rotate((float) angle, 1.0F, 0.0F, 0.0F);
        }
        GlStateManager.rotate(180, 1F, 0, 0);
        GLHelper.BLEND_SRC_ALPHA$ONE.blend();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.begin();
            ShaderLibrary.ALPHA.set("alpha", SkillRenderer.getDiffuseBlend(skillHolder.tick, skillHolder.data.time, 0.8F));
        }
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        this.bindTexture(TEXTURE);
        renderRing(scale, skillHolder.tick);
        renderRing(scale * 0.7, -skillHolder.tick);
        renderRing(scale * 0.5, skillHolder.tick);
        renderRing(scale * 0.3, -skillHolder.tick);
        renderRing(scale * 0.1, skillHolder.tick);

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
        GlStateManager.pushMatrix();
        GlStateManager.rotate((tick * 3) % 360F, 0F, 1F, 0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(scale, 0, -scale).tex(0, 0).endVertex();
        buffer.pos(scale, 0, scale).tex(1, 0).endVertex();
        buffer.pos(-scale, 0, scale).tex(1, 1).endVertex();
        buffer.pos(-scale, 0, -scale).tex(0, 1).endVertex();

        buffer.pos(-scale, 0, -scale).tex(0, 1).endVertex();
        buffer.pos(-scale, 0, scale).tex(1, 1).endVertex();
        buffer.pos(scale, 0, scale).tex(1, 0).endVertex();
        buffer.pos(scale, 0, -scale).tex(0, 0).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }
}
