package arekkuusu.enderskills.client.render.effect;

import arekkuusu.enderskills.client.util.helper.GLHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
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

public final class ParticleManager {

    private final List<ParticleBase> particles = new ArrayList<>();

    public void update() {
        Iterator<ParticleBase> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ParticleBase particle = iterator.next();
            if (!particle.isAlive()) iterator.remove();
            else particle.onUpdate();
        }
    }

    public void renderAll(float partial) {
        Entity entity = Minecraft.getMinecraft().player;
        if (entity != null) {
            Particle.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial;
            Particle.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial;
            Particle.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial;
            Particle.cameraViewDir = entity.getLook(partial);

            float x = ActiveRenderInfo.getRotationX();
            float z = ActiveRenderInfo.getRotationZ();
            float yz = ActiveRenderInfo.getRotationYZ();
            float xy = ActiveRenderInfo.getRotationXY();
            float xz = ActiveRenderInfo.getRotationXZ();

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();

            GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
            for (ParticleBase particle : particles) {
                if (!particle.isAdditive()) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    particle.renderParticle(buffer, entity, partial, x, xz, z, yz, xy);
                    tess.draw();
                }
            }

            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            for (ParticleBase particle : particles) {
                if (particle.isAdditive()) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    particle.renderParticle(buffer, entity, partial, x, xz, z, yz, xy);
                    tess.draw();
                }
            }

            GlStateManager.disableDepth();
            GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
            for (ParticleBase particle : particles) {
                if (!particle.isAdditive() && particle.shouldDisableDepth()) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    particle.renderParticle(buffer, entity, partial, x, xz, z, yz, xy);
                    tess.draw();
                }
            }

            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            for (ParticleBase particle : particles) {
                if (particle.isAdditive() && particle.shouldDisableDepth()) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    particle.renderParticle(buffer, entity, partial, x, xz, z, yz, xy);
                    tess.draw();
                }
            }
            GlStateManager.enableDepth();

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
        }
    }

    public void add(ParticleBase particle) {
        particles.add(particle);
    }
}
