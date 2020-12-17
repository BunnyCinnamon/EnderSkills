package arekkuusu.enderskills.client.render.tile;

import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileAltarRenderer extends TileEntitySpecialRenderer<TileAltar> {

    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation(LibMod.MOD_ID, "textures/entity/altar_book.png");
    private final ModelBook modelBook = new ModelBook();

    @Override
    public void render(TileAltar tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        //Render book
        if (MinecraftForgeClient.getRenderPass() == 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5f, y + 1.25f, z + 0.5f);
            float limbSwing = tile.tickCount + partialTicks;
            GlStateManager.translate(0, 0.1f + MathHelper.sin(limbSwing * 0.1f) * 0.01f, 0);
            float rotation = tile.bookRotation - tile.bookRotationPrev;
            while (rotation >= 3.1415927f) rotation -= 6.2831855f;
            while (rotation < -3.1415927f) rotation += 6.2831855f;
            GlStateManager.rotate(-(tile.bookRotationPrev + rotation * partialTicks) * 57.295776f, 0, 1, 0);
            GlStateManager.rotate(80, 0, 0, 1);
            this.bindTexture(TEXTURE_BOOK);
            float limbSwingAmount = tile.pageFlipPrev + (tile.pageFlip - tile.pageFlipPrev) * partialTicks + 0.25f;
            float ageInTicks = tile.pageFlipPrev + (tile.pageFlip - tile.pageFlipPrev) * partialTicks + 0.75f;
            limbSwingAmount = MathHelper.clamp((limbSwingAmount - MathHelper.fastFloor(limbSwingAmount)) * 1.6f - 0.3f, 0, 1);
            ageInTicks = MathHelper.clamp((ageInTicks - MathHelper.fastFloor(ageInTicks)) * 1.6f - 0.3f, 0, 1);
            GlStateManager.enableCull();
            modelBook.render(null, limbSwing, limbSwingAmount, ageInTicks, tile.bookSpreadPrev + (tile.bookSpread - tile.bookSpreadPrev) * partialTicks, 0, 0.0625f);
            GlStateManager.disableCull();
            GlStateManager.popMatrix();
            //Draw upgrade item
            if (tile.getLevel() == 1F && !tile.isUltimate() && tile.bookSpread > 0.5F) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", 0.5F);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.translate(x + 0.5, y + 1.45, z + 0.5);
                GlStateManager.rotate(tile.tickCount * 0.5F % 360F, 0F, 1F, 0F);
                RenderMisc.renderItemStack(new ItemStack(ModItems.CRYSTAL_MATRIX));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                ShaderLibrary.ALPHA.end();
            }
        }
        //Render Event
        if (MinecraftForgeClient.getRenderPass() == 1 && tile.lastLevelAnimationTimer > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5F, y + 0.01F, z + 0.5F);
            GlStateManager.enableBlend();
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                this.bindTexture(ResourceLibrary.DARK_BACKGROUND);
            } else {
                this.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
            }
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE.set("alpha", 0.9F * (tile.lastLevelAnimationTimer / 50F));
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F * (tile.lastLevelAnimationTimer / 50F));
                }
            }
            GL11.glEnable(3042);
            drawVoid();
            float scale = 1 + (tile.lastLevelAnimationTimer / 50F) * 4F;
            GlStateManager.scale(scale, scale, scale);
            RenderMisc.drawObj(0xFFFFFF, tile.lastLevelAnimationTimer / 50F, RenderMisc::drawSphereRaw);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        //Draw Name Plate
        if (!tile.isUltimate() && tile.getLevel() == 1F) {
            this.setLightmapDisabled(true);
            drawNameplate(tile, TextHelper.translate("altar_drop_line_0"), x, y + 0.75, z, 2);
            drawNameplate(tile, TextHelper.translate("altar_drop_line_1"), x, y + 0.5, z, 2);
            this.setLightmapDisabled(false);
        }
    }

    public void drawVoid() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        double width = 5;
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, -width).tex(0, 0).endVertex();
        buffer.pos(-width, 0, width).tex(0, 1).endVertex();
        buffer.pos(width, 0, width).tex(1, 1).endVertex();
        buffer.pos(width, 0, -width).tex(1, 0).endVertex();
        tessellator.draw();
    }
}