package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.ExtraJump;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ExtraJumpRenderer extends SkillRenderer<ExtraJump> {

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 0) {
            for (int i = 0; i < 16; i++) {
                if (entity.world.rand.nextDouble() < 0.8D) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                    double posY = vec.y + 0.1D * (entity.world.rand.nextDouble() - 0.5D);
                    double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, -0.05, 0), 4F, 18, 0xFFFFFF, 0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                }
            }
        }
    }
}
