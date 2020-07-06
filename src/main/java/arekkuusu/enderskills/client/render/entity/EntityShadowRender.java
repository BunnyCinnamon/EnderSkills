package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.common.entity.EntityShadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EntityShadowRender extends Render<EntityShadow> {

    public EntityShadowRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityShadow entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (MinecraftForgeClient.getRenderPass() == 1 && entity.fadedCountdown > 0) {
            UUID uuid = entity.getOwnerId();
            if (uuid != null) {
                Entity copy = entity.getEntityByUUID(uuid);
                if (copy != null) {
                    GlStateManager.pushAttrib();
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.color(0.4F, 0.4F, 0.4F, 0.5f * (entity.fadedCountdown / 30F));
                    Minecraft.getMinecraft().getRenderManager().renderEntity(copy, x, y, z, entity.rotationYaw, partialTicks, false);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    GlStateManager.popAttrib();
                }
            }
        }
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {

    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityShadow entity) {
        return null;
    }
}
