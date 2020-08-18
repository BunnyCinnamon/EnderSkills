package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.electric.Energize;
import arekkuusu.enderskills.common.skill.ability.defense.light.HealOther;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnergizeRenderer extends SkillRenderer<Energize> {

    public EnergizeRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.ENERGIZE, ProjectileElectric::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        Vec3d vec = entity.getPositionVector();
        double posX = vec.x;
        double posY = vec.y + entity.height + 0.5D;
        double posZ = vec.z;
        EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 4, 50, 0xFFA8A8, ResourceLibrary.PLUS);
    }
}
