package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.Smash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SmashRenderer extends SkillRenderer<Smash> {

    private static final ResourceLocation FOLLOWING_BOTTOM = new ResourceLocation(LibMod.MOD_ID, "textures/entity/smash_bottom.png");
    private static final ResourceLocation FOLLOWING_SIDES = new ResourceLocation(LibMod.MOD_ID, "textures/entity/smash_sides.png");

    public SmashRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.SMASH, Placeable::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.motionY > 0) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        double width = (entity.width / 2D) * 1.5D;
        double height = (entity.height / 2D) * 0.8D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        bindTexture(FOLLOWING_BOTTOM);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();

        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        tessellator.draw();
        bindTexture(FOLLOWING_SIDES);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        double u = entity.width / 1D;
        double v = (16D / 512D);
        double vMin = v * skillHolder.tick;
        double vMax = vMin + v;
        buffer.pos(-width, 0, -width).tex(0, vMax).endVertex();
        buffer.pos(width, 0, -width).tex(u, vMax).endVertex();
        buffer.pos(width, height, -width).tex(u, vMin).endVertex();
        buffer.pos(-width, height, -width).tex(0, vMin).endVertex();
        buffer.pos(-width, height, -width).tex(0, vMin).endVertex();
        buffer.pos(width, height, -width).tex(u, vMin).endVertex();
        buffer.pos(width, 0, -width).tex(u, vMax).endVertex();
        buffer.pos(-width, 0, -width).tex(0, vMax).endVertex();

        buffer.pos(width, 0, width).tex(0, vMax).endVertex();
        buffer.pos(-width, 0, width).tex(u, vMax).endVertex();
        buffer.pos(-width, height, width).tex(u, vMin).endVertex();
        buffer.pos(width, height, width).tex(0, vMin).endVertex();
        buffer.pos(width, height, width).tex(0, vMin).endVertex();
        buffer.pos(-width, height, width).tex(u, vMin).endVertex();
        buffer.pos(-width, 0, width).tex(u, vMax).endVertex();
        buffer.pos(width, 0, width).tex(0, vMax).endVertex();

        buffer.pos(-width, 0, width).tex(0, vMax).endVertex();
        buffer.pos(-width, 0, -width).tex(u, vMax).endVertex();
        buffer.pos(-width, height, -width).tex(u, vMin).endVertex();
        buffer.pos(-width, height, width).tex(0, vMin).endVertex();
        buffer.pos(-width, height, width).tex(0, vMin).endVertex();
        buffer.pos(-width, height, -width).tex(u, vMin).endVertex();
        buffer.pos(-width, 0, -width).tex(u, vMax).endVertex();
        buffer.pos(-width, 0, width).tex(0, vMax).endVertex();

        buffer.pos(width, 0, -width).tex(0, vMax).endVertex();
        buffer.pos(width, 0, width).tex(u, vMax).endVertex();
        buffer.pos(width, height, width).tex(u, vMin).endVertex();
        buffer.pos(width, height, -width).tex(0, vMin).endVertex();
        buffer.pos(width, height, -width).tex(0, vMin).endVertex();
        buffer.pos(width, height, width).tex(u, vMin).endVertex();
        buffer.pos(width, 0, width).tex(u, vMax).endVertex();
        buffer.pos(width, 0, -width).tex(0, vMax).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick == 0) {
                for (int t = 0; t < 4; t++) {
                    for (int u = 0; u < 2; u++) {
                        spawnSmoke(entity, entity.world.rand.nextDouble(), entity.world.rand.nextDouble());
                        spawnSmoke(entity, -entity.world.rand.nextDouble(), -entity.world.rand.nextDouble());
                        spawnSmoke(entity, entity.world.rand.nextDouble(), -entity.world.rand.nextDouble());
                        spawnSmoke(entity, -entity.world.rand.nextDouble(), entity.world.rand.nextDouble());
                    }
                }
            }
        }

        private static void spawnSmoke(Entity entity, double xVelocity, double zVelocity) {
            if(ClientProxy.canParticleSpawn()) {
                ParticleVanilla vanilla = new ParticleVanilla(entity.world, entity.getPositionVector(), new Vec3d(xVelocity, 0.05, zVelocity), 18F, 18, 0xFFFFFF, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
            }
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return null;
        }
    }
}
