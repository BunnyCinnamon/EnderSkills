package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Stunned;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StunnedRenderer extends SkillRenderer<Stunned> {

    private static final ResourceLocation SHADER = new ResourceLocation("shaders/post/desaturate.json");

    public StunnedRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        public boolean wasActive = false;

        @SubscribeEvent
        @SuppressWarnings("ConstantConditions")
        public void playerTick(TickEvent.ClientTickEvent event) {
            if (event.type == TickEvent.Type.CLIENT) {
                EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
                if (SkillHelper.isActive(Minecraft.getMinecraft().player, ModEffects.STUNNED)) {
                    if (!wasActive) {
                        renderer.loadShader(SHADER);
                        wasActive = true;
                    }
                } else if (wasActive) {
                    if (renderer.getShaderGroup() != null && renderer.getShaderGroup().getShaderGroupName() != null && SHADER.toString().equals(renderer.getShaderGroup().getShaderGroupName())) {
                        renderer.stopUseShader();
                    }
                    wasActive = false;
                }
            }
        }

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            if (SkillHelper.isActive(event.getEntity(), ModEffects.STUNNED)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.GRAY_SCALE.begin();
                }
            }
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            if (SkillHelper.isActive(event.getEntity(), ModEffects.STUNNED)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.GRAY_SCALE.end();
                }
            }
        }
    }
}
