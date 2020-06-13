package arekkuusu.enderskills.client.util.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

@SideOnly(Side.CLIENT)
public final class RenderMisc {

    public static int sphereId = 0;
    public static int cubeId = 0;

    public static void init() {
        Sphere sphere = new Sphere();
        sphereId = create(() -> {
            GlStateManager.rotate(90F, 1F, 0F, 0F);
            sphere.draw(1F, 32, 16);
            GlStateManager.rotate(-90F, 1F, 0F, 0F);
        });
        cubeId = create(() -> {
            drawCubeRaw();
        });
    }

    public static void drawObj(int color, float alpha, int id) {
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;
        GlStateManager.color(r, g, b, alpha);
        GlStateManager.callList(id);
    }

    public static void drawCubeRaw() {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder bb = tes.getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        //Front
        bb.pos(-0.5F, 0.5F, -0.5F).endVertex();
        bb.pos(0.5F, 0.5F, -0.5F).endVertex();
        bb.pos(0.5F, -0.5F, -0.5F).endVertex();
        bb.pos(-0.5F, -0.5F, -0.5F).endVertex();
        //Back
        bb.pos(0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(-0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(-0.5F, -0.5F, 0.5F).endVertex();
        bb.pos(0.5F, -0.5F, 0.5F).endVertex();
        //Right
        bb.pos(0.5F, 0.5F, -0.5F).endVertex();
        bb.pos(0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(0.5F, -0.5F, 0.5F).endVertex();
        bb.pos(0.5F, -0.5F, -0.5F).endVertex();
        //Left
        bb.pos(-0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(-0.5F, 0.5F, -0.5F).endVertex();
        bb.pos(-0.5F, -0.5F, -0.5F).endVertex();
        bb.pos(-0.5F, -0.5F, 0.5F).endVertex();
        //Top
        bb.pos(-0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(0.5F, 0.5F, 0.5F).endVertex();
        bb.pos(0.5F, 0.5F, -0.5F).endVertex();
        bb.pos(-0.5F, 0.5F, -0.5F).endVertex();
        //Bottom
        bb.pos(-0.5F, -0.5F, -0.5F).endVertex();
        bb.pos(0.5F, -0.5F, -0.5F).endVertex();
        bb.pos(0.5F, -0.5F, 0.5F).endVertex();
        bb.pos(-0.5F, -0.5F, 0.5F).endVertex();
        tes.draw();
    }

    public static int create(Runnable r) {
        int res = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(res, GL11.GL_COMPILE);
        r.run();
        GlStateManager.glEndList();
        return res;
    }

    public static float getRenderWorldTime(float partialTicks) {
        return (Minecraft.getSystemTime() + partialTicks) / 20F;
    }

    public static float getRenderPlayerTime() {
        return Minecraft.getMinecraft().player.ticksExisted;
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        float r = (color >>> 24 & 0xFF) / 256F;
        float g = (color >>> 16 & 0xFF) / 256F;
        float b = (color >>> 8 & 0xFF) / 256F;
        float a = (color & 0xFF) / 256F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public static void draw8Rect(int x, int y, int width, int height) {
        //Corners
        Gui.drawScaledCustomSizeModalRect(x, y, 0, 155, 3, 3, 3, 3, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x + width - 3, y, 6, 155, 3, 3, 3, 3, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x, y + height - 3, 0, 161, 3, 3, 3, 3, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x + width - 3, y + height - 3, 6, 161, 3, 3, 3, 3, 256, 256);
        //Sides
        Gui.drawScaledCustomSizeModalRect(x + 3, y, 4, 155, 1, 3, width - 6, 3, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x, y + 3, 0, 159, 3, 1, 3, height - 6, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x + width - 3, y + 3, 6, 159, 3, 1, 3, height - 6, 256, 256);
        Gui.drawScaledCustomSizeModalRect(x + 3, y + height - 3, 4, 161, 1, 3, width - 6, 3, 256, 256);
        //Inside
        Gui.drawScaledCustomSizeModalRect(x + 3, y + 3, 4, 159, 1, 1, width - 6, height - 6, 256, 256);
    }

    public static void renderEntityOnFire(Entity entity, double x, double y, double z) {
        GlStateManager.disableLighting();
        TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
        TextureAtlasSprite textureatlassprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        float f = entity.width * 1.4F;
        GlStateManager.scale(f, f, f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = entity.height / f;
        float f4 = (float) (entity.posY - entity.getEntityBoundingBox().minY);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, -0.3F + (float) ((int) f3) * 0.02F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f5 = 0.0F;
        int i = 0;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        while (f3 > 0.0F) {
            TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            float f6 = textureatlassprite2.getMinU();
            float f7 = textureatlassprite2.getMinV();
            float f8 = textureatlassprite2.getMaxU();
            float f9 = textureatlassprite2.getMaxV();

            if (i / 2 % 2 == 0) {
                float f10 = f8;
                f8 = f6;
                f6 = f10;
            }

            bufferbuilder.pos((f1 - 0.0F), (0.0F - f4), f5).tex(f8, f9).endVertex();
            bufferbuilder.pos((-f1 - 0.0F), (0.0F - f4), f5).tex(f6, f9).endVertex();
            bufferbuilder.pos((-f1 - 0.0F), (1.4F - f4), f5).tex(f6, f7).endVertex();
            bufferbuilder.pos((f1 - 0.0F), (1.4F - f4), f5).tex(f8, f7).endVertex();
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 += 0.03F;
            ++i;
        }

        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    public static void renderItemStack(ItemStack stack) {
        //Fix stack 'y' center
        if(stack.getItem() instanceof ItemBlock) {
            GlStateManager.translate(0F, -0.1F, 0F);
        }
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableRescaleNormal();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderItem render = Minecraft.getMinecraft().getRenderItem();
        IBakedModel model = render.getItemModelWithOverrides(stack, null, null);
        IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
        render.renderItem(stack, transformedModel);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableRescaleNormal();
    }

    public static void makeUpDownTranslation(float tick, float max, float speed) {
        GlStateManager.translate(0, getInterpolated(tick, max, speed), 0);
    }

    public static double getInterpolated(float tick, float max, float speed) {
        float angle = 0;
        double toDegrees = Math.PI / 180D;
        angle += speed * tick;
        if (angle > 360) angle -= 360;
        return max * Math.sin(angle * toDegrees);
    }
}
