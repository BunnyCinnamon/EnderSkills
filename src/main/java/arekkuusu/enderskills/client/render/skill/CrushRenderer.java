package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Crush;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CrushRenderer extends SkillRenderer<Crush> {

    public CrushRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.CRUSH, ProjectileWindRenderer::new);
    }
}
