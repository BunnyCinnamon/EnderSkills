package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class PotionBase extends Potion {

    public final int iconIndex;

    public PotionBase(String name, int color, int iconIndex) {
        super(false, color);
        setRegistryName(LibMod.MOD_ID, "potion_" + name);
        setPotionName(LibMod.MOD_ID + ".potion_" + name);
        this.iconIndex = iconIndex;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        this.onUpdate(entityLivingBaseIn, amplifier);
    }

    public void onUpdate(EntityLivingBase entity, int amplifier) {
        //For-Rent
    }

    @Override
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
        this.onApply(entityLivingBaseIn, amplifier);
    }

    public void onApply(EntityLivingBase entity, int amplifier) {
        //For-Rent
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        render(x + 3, y + 3, alpha);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        render(x + 6, y + 7, 1);
    }

    @SideOnly(Side.CLIENT)
    private void render(int x, int y, float alpha) {
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLibrary.POTION_TEXTURES);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(7, DefaultVertexFormats.POSITION_TEX);
        GlStateManager.color(1, 1, 1, alpha);

        int textureX = iconIndex % 8 * 18;
        int textureY = 198 + iconIndex / 8 * 18;
        float f = 0.00390625F; //What is this for?

        buf.pos(x, y + 18D, 0).tex(textureX * f, (textureY + 18) * f).endVertex();
        buf.pos(x + 18D, y + 18D, 0).tex((textureX + 18) * f, (textureY + 18) * f).endVertex();
        buf.pos(x + 18D, y, 0).tex((textureX + 18) * f, textureY * f).endVertex();
        buf.pos(x, y, 0).tex(textureX * f, textureY * f).endVertex();

        tessellator.draw();
    }
}
