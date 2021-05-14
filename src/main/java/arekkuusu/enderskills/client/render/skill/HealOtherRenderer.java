package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.light.HealOther;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HealOtherRenderer extends SkillRenderer<HealOther> {

    public HealOtherRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.HEAL_OTHER, ProjectileLightRenderer::new);
    }
}
