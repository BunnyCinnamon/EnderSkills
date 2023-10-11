package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ability.offence.blackflame.BlackBlessingFlame;
import arekkuusu.enderskills.common.skill.ability.offence.fire.FireSpirit;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlackBlessingFlameRenderer extends SkillRenderer<BlackBlessingFlame> {

    private static final ResourceLocation FOLLOWING = new ResourceLocation(LibMod.MOD_ID, "textures/entity/fire_spirit_.png");

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        Entity owner = NBTHelper.getEntity(EntityLivingBase.class, skillHolder.data.nbt, "owner");
        if (owner == entity) {
            for (int i = -1; i <= 2; i++) {
                if (i == 0) continue;
                GlStateManager.pushMatrix();
                GLHelper.BLEND_SRC_ALPHA$ONE.blend();
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.ALPHA.begin();
                    ShaderLibrary.ALPHA.set("alpha", 0.8F);
                }
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.translate(x, y + entity.height / 2, z);
                GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) (this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((entity.ticksExisted * i * 2) % 360F, 0F, 1F, 0F);
                GlStateManager.rotate((entity.ticksExisted * i * 2) % 720F, 1F, 0F, 0F);
                GlStateManager.rotate((entity.ticksExisted * i * 2) % 360F, 0F, 0F, 1F);
                this.bindTexture(FOLLOWING);
                float size = entity.height / 2 * 1.5F;
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-size, -size, 0).tex(0, 0).endVertex();
                buffer.pos(size, -size, 0).tex(1, 0).endVertex();
                buffer.pos(size, size, 0).tex(1, 1).endVertex();
                buffer.pos(-size, size, 0).tex(0, 1).endVertex();
                buffer.pos(-size, size, 0).tex(0, 1).endVertex();
                buffer.pos(size, size, 0).tex(1, 1).endVertex();
                buffer.pos(size, -size, 0).tex(1, 0).endVertex();
                buffer.pos(-size, -size, 0).tex(0, 0).endVertex();
                tessellator.draw();
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.ALPHA.end();
                }
                GLHelper.BLEND_NORMAL.blend();
                GlStateManager.popMatrix();
            }
        } else {
            RenderMisc.renderEntityOnFire(entity, x, y, z);
        }
    }
}
