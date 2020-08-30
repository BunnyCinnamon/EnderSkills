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
import arekkuusu.enderskills.common.skill.ability.defense.electric.ShockingAura;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ShockingAuraRenderer extends SkillRenderer<ShockingAura> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/electric_ring.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        int tick = Math.min(skillHolder.tick, EntityPlaceableData.MIN_TIME);
        double scale = NBTHelper.getDouble(skillHolder.data.nbt, "range") * ((double) tick / (double) EntityPlaceableData.MIN_TIME);

        GlStateManager.pushMatrix();
        GlStateManager.color(0.608F, 0.508F, 0.19F, 1F);
        SpriteLibrary.ELECTRIC_RING.bind();
        GLHelper.BLEND_SRC_ALPHA$ONE.blend();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.begin();
            ShaderLibrary.ALPHA.set("alpha", 1F);
        }
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.translate(x, y, z);
        //Top
        renderFence(entity, 0, ((float) scale) + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 2F, 5, scale);
        renderFence(entity, 0, (((float) scale) + 0.2F) - (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, -1F, 2, scale);
        renderFence(entity, 0, (((float) scale)) - (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, -2F, 3, scale);
        //Bottom
        renderFence(entity, 0, -0.15F + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 1.5F, 1, scale);
        renderFence(entity, 0, -(((float) scale)) + (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, 2.5F, 6, scale);
        renderFence(entity, 0, -(((float) scale) + 0.2F) - (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, -1.5F, 4, scale);
        renderFence(entity, 0, -(((float) scale)) - (((entity.ticksExisted + partialTicks) % 10F) / 10F) * 0.5F, 0, partialTicks, -2.5F, 8, scale);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.end();
        }
        GLHelper.BLEND_NORMAL.blend();
        GlStateManager.popMatrix();
    }

    public void renderFence(Entity entity, float x, float y, float z, float partialTicks, float angleOffset, int textureOffset, double scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2, z);

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
}
