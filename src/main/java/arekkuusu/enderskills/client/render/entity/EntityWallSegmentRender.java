package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.common.entity.EntityWallSegment;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class EntityWallSegmentRender extends Render<EntityWallSegment> {

    public EntityWallSegmentRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityWallSegment entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IBlockState[] states = entity.getBlocks();
        for (int i = 0; i < states.length; i++) {
            IBlockState state = states[i];
            if (state != null) renderBlock(state, entity, x, y + i, z, new BlockPos(entity).up(i));
        }
    }

    private void renderBlock(IBlockState blockState, EntityWallSegment entity, double x, double y, double z,
                             BlockPos pos) {
        Tessellator tessellator = Tessellator.getInstance();

        if (blockState.getRenderType() == EnumBlockRenderType.MODEL) {

            if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                BufferBuilder vb = tessellator.getBuffer();
                vb.begin(7, DefaultVertexFormats.BLOCK);
                GlStateManager.translate(x - pos.getX() - 0.5, y - pos.getY(), z - pos.getZ() - 0.5);
                BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();

                GlStateManager.translate(0, -1, 0);
                brd.getBlockModelRenderer().renderModel(entity.world, brd.getModelForState(blockState),
                        blockState, pos.up(), vb, false, 0);
                tessellator.draw();

                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {

    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityWallSegment entity) {
        return null;
    }
}
