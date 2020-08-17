package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.defense.light.NearbyInvincibility;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class NearbyInvincibilityRenderer extends SkillRenderer<NearbyInvincibility> {

    private static final ResourceLocation FOLLOWING = new ResourceLocation(LibMod.MOD_ID, "textures/entity/nearby_invincibility.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.translate(x, y + entity.height / 2, z);
        GlStateManager.rotate(180, 1F, 0, 0);
        GlStateManager.rotate(entity.ticksExisted * 0.75F % 360F, 0F, 1F, 0F);
        GLHelper.BLEND_SRC_ALPHA$ONE.blend();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.begin();
            ShaderLibrary.ALPHA.set("alpha", SkillRenderer.getDiffuseBlend(skillHolder.tick, skillHolder.data.time, 0.5F));
        }
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        double scale = NBTHelper.getDouble(skillHolder.data.nbt, "range") * ((double) skillHolder.tick / (double) skillHolder.data.time);
        if (skillHolder.tick % 5 == 0) {
            if (entity.world.rand.nextDouble() < 0.2D && ClientProxy.canParticleSpawn()) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + scale * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + (entity.height / 2) + scale * (entity.world.rand.nextDouble() - 0.5);
                double posZ = vec.z + scale * (entity.world.rand.nextDouble() - 0.5);
                EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 1F, 50, 0xFFFFFF, ResourceLibrary.PLUS);
            }
        }
        this.bindTexture(FOLLOWING);
        drawSquareWithOffset(scale, scale);
        drawSquareWithOffset(scale, 0);
        drawSquareWithOffset(scale, -scale);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
            ShaderLibrary.ALPHA.end();
        }
        GLHelper.BLEND_NORMAL.blend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void drawSquareWithOffset(double scale, double offset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, offset, 0);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(scale, 0, -scale).tex(1, 0).endVertex();
        buffer.pos(scale, 0, scale).tex(1, 1).endVertex();
        buffer.pos(-scale, 0, scale).tex(0, 1).endVertex();
        buffer.pos(-scale, 0, -scale).tex(0, 0).endVertex();

        buffer.pos(-scale, 0, -scale).tex(0, 0).endVertex();
        buffer.pos(-scale, 0, scale).tex(0, 1).endVertex();
        buffer.pos(scale, 0, scale).tex(1, 1).endVertex();
        buffer.pos(scale, 0, -scale).tex(1, 0).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }
}
