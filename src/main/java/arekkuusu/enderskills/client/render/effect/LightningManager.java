package arekkuusu.enderskills.client.render.effect;

import arekkuusu.enderskills.client.util.helper.GLHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LightningManager {

    private final List<Lightning> lightnings = new ArrayList<>();

    public void update() {
        Iterator<Lightning> iterator = lightnings.iterator();
        while (iterator.hasNext()) {
            Lightning particle = iterator.next();
            if (!particle.isAlive()) iterator.remove();
            else particle.onUpdate();
        }
    }

    public void renderAll(float partial) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) entity = Minecraft.getMinecraft().player;
        if (entity != null) {
            double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partial);
            double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partial);
            double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partial);
            GlStateManager.translate(-tx, -ty, -tz);

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();

            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            for (Lightning lightning : lightnings) {
                lightning.renderParticle(buffer, partial);
            }
            tess.draw();

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
        }
    }

    public void add(Lightning particle) {
        lightnings.add(particle);
        particle.make();
    }
}
