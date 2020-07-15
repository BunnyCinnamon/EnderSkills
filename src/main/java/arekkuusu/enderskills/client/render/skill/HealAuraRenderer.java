package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.light.HealAura;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class HealAuraRenderer extends SkillRenderer<HealAura> {

    private static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/heal_aura.png");

    public HealAuraRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.HEAL_AURA, Placeable::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick % 5 == 0) {
            for (int i = 0; i < 3; i++) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + entity.width * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + entity.height * entity.world.rand.nextDouble();
                double posZ = vec.z + entity.width * (entity.world.rand.nextDouble() - 0.5);
                EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 3, 50, 0x58DB11, ResourceLibrary.PLUS);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (MinecraftForgeClient.getRenderPass() != 1) return;
            GlStateManager.color(1F, 1F, 1F, 1F);
            if (entity.tick % 5 == 0) {
                for (int i = 0; i < 6; i++) {
                    if (entity.world.rand.nextDouble() < 0.4D) {
                        Vec3d vec = entity.getPositionVector();
                        double posX = vec.x + entity.width * (entity.world.rand.nextDouble() - 0.5);
                        double posY = vec.y + entity.height * (entity.world.rand.nextDouble() - 0.5);
                        double posZ = vec.z + entity.width * (entity.world.rand.nextDouble() - 0.5);
                        EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 1, 50, 0xFFFFFF, ResourceLibrary.PLUS);
                    }
                }
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            ShaderLibrary.BRIGHT.begin();
            ShaderLibrary.BRIGHT.set("alpha", SkillRenderer.getBlend(entity.tick, entity.getLifeTime(), 0.8F));
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            this.bindTexture(getEntityTexture(entity));
            int tick = Math.min(entity.tick, EntityPlaceableData.MIN_TIME);
            double scale = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            double offset = entity.getRadius() * ((double) tick / (double) EntityPlaceableData.MIN_TIME);
            drawSquareWithOffset(scale, offset);
            drawSquareWithOffset(scale, 0);
            drawSquareWithOffset(scale, -offset);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            ShaderLibrary.BRIGHT.end();
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

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return PLACEABLE;
        }
    }
}
