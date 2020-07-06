package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGrasp;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.ender.Grasp;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GraspRenderer extends SkillRenderer<Grasp> {

    public GraspRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.GRASP, ProjectileVoid::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        EntityPlaceableGrasp grasp = NBTHelper.getEntity(EntityPlaceableGrasp.class, skillHolder.data.nbt, "grasp");
        if (grasp != null && skillHolder.tick % 2 == 0) {
            Vec3d vec = new Vec3d(entity.width / 2, 0, entity.width / 2);
            if (!entity.onGround) {
                BlockPos entityPos = entity.getPosition();
                for (int i = 1; i <= 2; i++) {
                    BlockPos pos = entityPos.down(i);
                    if (isSolid(entity.world, pos)) {
                        vec = new Vec3d(vec.x, -(entity.posY - pos.getY()) + 1, vec.z);
                        break;
                    }
                }
            }
            spawnParticle(vec, entity);
            vec = vec.rotateYaw(120 * (float) Math.PI / 180F);
            spawnParticle(vec, entity);
            vec = vec.rotateYaw(120F * (float) Math.PI / 180F);
            spawnParticle(vec, entity);
        }
    }

    public boolean isSolid(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
    }

    public void spawnParticle(Vec3d vec, Entity entity) {
        float angle = entity.ticksExisted * 1.15F % 360F;
        vec = vec.rotateYaw(angle * (float) Math.PI / 180F);
        vec = vec.add(entity.getPositionVector());
        ES.getProxy().spawnParticle(entity.world, vec, new Vec3d(0, 0.1, 0), 3F, 50, 0x1E0034, ResourceLibrary.GLOW_PARTICLE_EFFECT);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableGrasp> {

        public final int[] colors = new int[]{
                0x1E0034,
                0x260742,
                0x30104F,
                0x38185B,
                0x401E68,
                0x472476,
                0x4E2A84,
                0x5B3C8C,
                0x684C96,
                0x765D9F,
                0x836EA9,
        };

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableGrasp entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            Vec3d originVec = entity.getPositionVector();
            for (BlockPos pos : entity.getTerrainBlocks()) {
                if (entity.ticksExisted % 5 == 0 && entity.world.rand.nextDouble() < 0.005D) {
                    double posX = pos.getX() + 1 * entity.world.rand.nextDouble();
                    double posY = pos.getY() + 1D + 0.1 * entity.world.rand.nextDouble();
                    double posZ = pos.getZ() + 1 * entity.world.rand.nextDouble();
                    ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 2F, 50, colors[entity.world.rand.nextInt(colors.length - 1)], ResourceLibrary.GLOW_PARTICLE_EFFECT);
                }
                GlStateManager.pushMatrix();
                GlStateManager.translate(x - originVec.x, y - originVec.y, z - originVec.z);
                if (!CommonConfig.RENDER_CONFIG.rendering.vanilla || CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                    this.bindTexture(ResourceLibrary.DARK_BACKGROUND);
                } else {
                    this.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
                }
                GlStateManager.enableBlend();
                if (!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                    if (!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                        ShaderLibrary.UNIVERSE.begin();
                        ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                        ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                        ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                        ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                        ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                        ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
                    } else {
                        ShaderLibrary.UNIVERSE_DEFAULT.begin();
                        ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                        ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                        ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                        ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
                    }
                }
                double yOffset = 0.005D;
                GL11.glEnable(3042);
                drawVoid(pos, yOffset);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(3042);
                GlStateManager.disableBlend();
                if (!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                    if (!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                        ShaderLibrary.UNIVERSE.end();
                    } else {
                        ShaderLibrary.UNIVERSE_DEFAULT.end();
                    }
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }

        public void drawVoid(BlockPos pos, double yOffset) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.pushMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            double uMin = 0;
            double vMin = 0;
            double uMax = 1;
            double vMax = 1;
            double xPos = pos.getX();
            double yPos = pos.getY() + 1;
            double zPos = pos.getZ();
            double width = 1;
            buffer.pos(xPos + width, yPos + yOffset, zPos).tex(uMax, vMin).endVertex();
            buffer.pos(xPos + width, yPos + yOffset, zPos + 1).tex(uMax, vMax).endVertex();
            buffer.pos(xPos, yPos + yOffset, zPos + 1).tex(uMin, vMax).endVertex();
            buffer.pos(xPos, yPos + yOffset, zPos).tex(uMin, vMin).endVertex();
            buffer.pos(xPos, yPos + yOffset, zPos).tex(uMin, vMin).endVertex();
            buffer.pos(xPos, yPos + yOffset, zPos + 1).tex(uMin, vMax).endVertex();
            buffer.pos(xPos + width, yPos + yOffset, zPos + 1).tex(uMax, vMax).endVertex();
            buffer.pos(xPos + width, yPos + yOffset, zPos).tex(uMax, vMin).endVertex();
            tessellator.draw();

            GlStateManager.depthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(xPos + width, yPos + yOffset + 0.01, zPos).tex(uMax, vMin).endVertex();
            buffer.pos(xPos + width, yPos + yOffset + 0.01, zPos + 1).tex(uMax, vMax).endVertex();
            buffer.pos(xPos, yPos + yOffset + 0.01, zPos + 1).tex(uMin, vMax).endVertex();
            buffer.pos(xPos, yPos + yOffset + 0.01, zPos).tex(uMin, vMin).endVertex();
            buffer.pos(xPos, yPos + yOffset + 0.01, zPos).tex(uMin, vMin).endVertex();
            buffer.pos(xPos, yPos + yOffset + 0.01, zPos + 1).tex(uMin, vMax).endVertex();
            buffer.pos(xPos + width, yPos + yOffset + 0.01, zPos + 1).tex(uMax, vMax).endVertex();
            buffer.pos(xPos + width, yPos + yOffset + 0.01, zPos).tex(uMax, vMin).endVertex();
            tessellator.draw();
            GlStateManager.depthMask(true);

            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableGrasp entity) {
            return TextureMap.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
