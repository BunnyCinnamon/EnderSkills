package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.common.skill.effect.Burning;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BurningRenderer extends SkillRenderer<Burning> {

    public BurningRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent()
        public void onLivingPreRender(RenderLivingEvent.Post<EntityLivingBase> event) {
        }
    }
}
