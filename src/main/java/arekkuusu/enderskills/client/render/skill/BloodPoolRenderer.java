package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableBloodPool;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blood.BloodPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class BloodPoolRenderer extends SkillRenderer<BloodPool> {

    public BloodPoolRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.BLOOD_POOL, ProjectileBlood::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 5 == 0 && entity.world.rand.nextDouble() < 0.1D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, -0.01, 0), 2F, 50, 0x690303, ResourceLibrary.DROPLET);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableBloodPool> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableBloodPool entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Vec3d originVec = entity.getPositionVector();
            for (BlockPos pos : entity.getTerrainBlocks()) {
                if (entity.ticksExisted % 5 == 0 && entity.world.rand.nextDouble() < 0.005D) {
                    double posX = pos.getX() + 1 * entity.world.rand.nextDouble();
                    double posY = pos.getY() + 1D + 0.1 * entity.world.rand.nextDouble();
                    double posZ = pos.getZ() + 1 * entity.world.rand.nextDouble();
                    EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.05, 0), 2F, 50, 0x690303, ResourceLibrary.DROPLET);
                }
                GlStateManager.pushMatrix();
                GlStateManager.translate(x - originVec.x, y - originVec.y, z - originVec.z);
                this.bindTexture(getEntityTexture(entity));
                TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ResourceLibrary.BLOOD.toString());
                if (sprite == null) sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                float fading = MathHelper.clamp(1F - (float) (entity.getPosition().getDistance(pos.getX(), pos.getY(), pos.getZ()) / (entity.getRadius() * 1.5D)), 0F, 1F);
                double yOffset = 0.005D;
                double xPos = pos.getX();
                double yPos = pos.getY() + 1;
                double zPos = pos.getZ();
                double uMin = sprite.getMinU();
                double vMin = sprite.getMinV();
                double uMax = sprite.getMaxU();
                double vMax = sprite.getMaxV();
                int color = 0xFFFFFFFF;
                int a = (int) ((color >> 24 & 0xFF) * fading);
                int r = color >> 16 & 0xFF;
                int g = color >> 8 & 0xFF;
                int b = color & 0xFF;
                int brightness = Minecraft.getMinecraft().world.getCombinedLight(pos.up(), 0);
                int light1 = brightness >> 0x10 & 0xFFFF;
                int light2 = brightness & 0xFFFF;
                double width = 1;
                buffer.pos(xPos + width, yPos + yOffset, zPos).color(r, g, b, a).tex(uMax, vMin).lightmap(light1, light2).endVertex();
                buffer.pos(xPos + width, yPos + yOffset, zPos + 1).color(r, g, b, a).tex(uMax, vMax).lightmap(light1, light2).endVertex();
                buffer.pos(xPos, yPos + yOffset, zPos + 1).color(r, g, b, a).tex(uMin, vMax).lightmap(light1, light2).endVertex();
                buffer.pos(xPos, yPos + yOffset, zPos).color(r, g, b, a).tex(uMin, vMin).lightmap(light1, light2).endVertex();
                buffer.pos(xPos, yPos + yOffset, zPos).color(r, g, b, a).tex(uMin, vMin).lightmap(light1, light2).endVertex();
                buffer.pos(xPos, yPos + yOffset, zPos + 1).color(r, g, b, a).tex(uMin, vMax).lightmap(light1, light2).endVertex();
                buffer.pos(xPos + width, yPos + yOffset, zPos + 1).color(r, g, b, a).tex(uMax, vMax).lightmap(light1, light2).endVertex();
                buffer.pos(xPos + width, yPos + yOffset, zPos).color(r, g, b, a).tex(uMax, vMin).lightmap(light1, light2).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.disableBlend();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableBloodPool entity) {
            return TextureMap.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
