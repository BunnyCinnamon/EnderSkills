package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Blinded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

@SideOnly(Side.CLIENT)
public class BlindedRenderer extends SkillRenderer<Blinded> {

    public BlindedRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        public int tick = -1;

        @SubscribeEvent
        public void onFogDensityRender(EntityViewRenderEvent.FogDensity event) {
            boolean active = SkillHelper.isActive(event.getEntity(), ModEffects.BLINDED);
            if (active && tick == -1) tick = 10;
            else if (!active && tick > 0) tick--;
            else if (tick == 0) tick = -1;

            if (active || tick > 0) {
                float f1 = 2F;
                if (tick < 10) {
                    f1 = f1 + (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 - f1) * (1F - (float) tick / 10F);
                }
                GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
                GlStateManager.setFogStart(f1 * 0.25F);
                GlStateManager.setFogEnd(f1);
                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GlStateManager.glFogi(34138, 34139);
                }
                event.setDensity(1F);
                event.setCanceled(true);
            }
        }
    }
}
