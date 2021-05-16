package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.Invisibility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InvisibilityRenderer extends SkillRenderer<Invisibility> {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (SkillHelper.isActive(event.getEntity(), ModAbilities.INVISIBILITY)) {
            event.setCanceled(true);
        }
    }
}
