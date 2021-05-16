package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.electric.Energize;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnergizeRenderer extends SkillRenderer<Energize> {

    public EnergizeRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.ENERGIZE, ProjectileElectricRenderer::new);
    }
}
