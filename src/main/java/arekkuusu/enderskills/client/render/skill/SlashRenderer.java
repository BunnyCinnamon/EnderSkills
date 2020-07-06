package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Slash;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class SlashRenderer extends SkillRenderer<Slash> {

    public static final ResourceLocation PLACEABLE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/slash.png");

    public SlashRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.SLASH, Placeable::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            /*if (entity.tick == 1*//*entity.tick % 2 == 0*//*) {
             *//*double distance = entity.getRadius()*//**//* * ((float) entity.tick / (float) entity.getLifeTime())*//**//*;
                Vec3d lookVec = entity.getLookVec();
                *//**//*Quat quatX = Quat.fromAxisAngleRad(Vector.X, (entity.world.rand.nextFloat() - 0.5F) * 40F * (float) Math.PI / 180F);
                Quat quatY = Quat.fromAxisAngleRad(Vector.Y, (entity.world.rand.nextFloat() - 0.5F) * 40F * (float) Math.PI / 180F);
                Quat quatZ = Quat.fromAxisAngleRad(Vector.Z, (entity.world.rand.nextFloat() - 0.5F) * 40F * (float) Math.PI / 180F);
                Quat rotation = quatX.multiply(quatY).multiply(quatZ);
                lookVec = new Vector(lookVec).rotate(rotation).toVec3d();*//**//*
                Vec3d posVec = entity.getPositionVector();
                Vec3d offsetVec = posVec.addVector(
                        lookVec.x * distance,
                        lookVec.y * distance,
                        lookVec.z * distance
                );
                Vec3d motion = posVec.subtract(offsetVec).scale(-1);
                motion = new Vec3d(motion.x / 10, motion.y / 10, motion.z / 10);
                *//**//*for (int i = 0; i < 2; ++i) {
                    for (int j = 1; j < 4; ++j) {*//**//*
                        ParticleSweepVanilla vanilla = new ParticleSweepVanilla(entity.world, posVec, motion*//**//*new Vec3d(0, 0, 0)*//**//*, 15F, 10, 0xFFFFFF);
                        //ParticleVanilla vanilla = new ParticleVanilla(entity.world, offsetVec, new Vec3d(0, 0, 0), 15F, 10, 0xFFFFFF, 0);
                        Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                    *//**//*}
                }*//*
            }*/
            if (entity.tick > entity.getLifeTime()) return;
            double progress = ((double) entity.tick + partialTicks) / (double) entity.getLifeTime();
            double scale = (entity.getRadius() + 3) * 2 * progress;
            GlStateManager.pushMatrix();
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            ShaderLibrary.BRIGHT.begin();
            ShaderLibrary.BRIGHT.set("alpha", 0.6F * (float) progress);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.translate(x, y, z);
            GlStateManager.scale(scale, scale, scale);
            {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(entity.rotationYaw, 0F, -1F, 0F);
                GlStateManager.rotate(entity.rotationPitch - 35F, 1F, 0F, 0F);
                this.bindTexture(getEntityTexture(entity));
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                double yOffset = -(0.5D - 0.25D * progress);
                buffer.pos(-0.5D, yOffset, -0.5D).tex(0, 0).endVertex();
                buffer.pos(0.5D, yOffset, -0.5D).tex(1, 0).endVertex();
                buffer.pos(0.5D, yOffset, 0.5D).tex(1, 1).endVertex();
                buffer.pos(-0.5D, yOffset, 0.5D).tex(0, 1).endVertex();

                buffer.pos(-0.5D, yOffset, 0.5D).tex(0, 1).endVertex();
                buffer.pos(0.5D, yOffset, 0.5D).tex(1, 1).endVertex();
                buffer.pos(0.5D, yOffset, -0.5D).tex(1, 0).endVertex();
                buffer.pos(-0.5D, yOffset, -0.5D).tex(0, 0).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            ShaderLibrary.BRIGHT.end();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            return PLACEABLE;
        }
    }
}
