package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.Invisibility;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InvisibilityRenderer extends SkillRenderer<Invisibility> {

    public InvisibilityRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void renderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            Capabilities.get(event.getEntity()).ifPresent(capability -> {
                if (capability.isActive(ModAbilities.INVISIBILITY)) {
                    event.setCanceled(true);
                }
            });
        }

        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void renderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            Capabilities.get(event.getEntity()).ifPresent(capability -> {
                if (capability.isActive(ModAbilities.INVISIBILITY)) {
                    event.setCanceled(true);
                }
            });
        }
    }
}
