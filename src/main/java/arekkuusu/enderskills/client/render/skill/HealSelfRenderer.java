package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.skill.ability.defense.light.HealSelf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HealSelfRenderer extends SkillRenderer<HealSelf> {

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick % 5 == 0) {
            for (int i = 0; i < 6; i++) {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5);
                double posY = vec.y + entity.height * entity.world.rand.nextDouble();
                double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5);
                ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 3, 50, 0x58DB11, ResourceLibrary.PLUS);
            }
        }
    }
}
