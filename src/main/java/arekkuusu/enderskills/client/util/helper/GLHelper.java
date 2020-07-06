package arekkuusu.enderskills.client.util.helper;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public enum GLHelper {
    BLEND_NORMAL(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA),
    BLEND_ALPHA(GL11.GL_ONE, GL11.GL_SRC_ALPHA),
    BLEND_PRE_ALPHA(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA),
    BLEND_MULTIPLY(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA),
    BLEND_ADDITIVE(GL11.GL_ONE, GL11.GL_ONE),
    BLEND_ADDITIVE_DARK(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR),
    BLEND_OVERLAY_DARK(GL11.GL_SRC_COLOR, GL11.GL_ONE),
    BLEND_ADDITIVE_ALPHA(GL11.GL_SRC_ALPHA, GL11.GL_ONE),
    BLEND_INVERTED_ADD(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR),
    BLEND_SRC_ALPHA$ONE(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE),
    BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

    private final int gl0, gl1;

    GLHelper(int gl0, int gl1) {
        this.gl0 = gl0;
        this.gl1 = gl1;
    }

    GLHelper(GlStateManager.SourceFactor factor, GlStateManager.DestFactor dest) {
        this.gl0 = factor.factor;
        this.gl1 = dest.factor;
    }

    public void blend() {
        GlStateManager.blendFunc(gl0, gl1);
    }

    public static void lightMap(float u, float v) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, u, v);
    }

    public static void disableDepth() {
        GlStateManager.depthMask(false);
    }

    public static void enableDepth() {
        GlStateManager.depthMask(true);
    }
}
