package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.Dash;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DashRenderer extends SkillRenderer<Dash> {

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 0) {
            for (int i = 0; i < 16; i++) {
                if (entity.world.rand.nextDouble() < 0.8D) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                    double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
                    double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(-entity.motionX, -entity.motionY, -entity.motionZ), 3F, 18, 0xFFFFFF, 0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                }
            }
        }
        if (entity.onGround) {
            Vec3d pos = entity.getPositionVector();
            IBlockState state = entity.world.getBlockState(new BlockPos(pos).down());
            for (int i = 0; i < 12; i++) {
                if (entity.world.rand.nextDouble() < 0.3D) {
                    double posX = pos.x + entity.world.rand.nextDouble() - 0.5;
                    double posY = pos.y;
                    double posZ = pos.z + entity.world.rand.nextDouble() - 0.5;
                    entity.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX, posY, posZ, 0.0D, 0.01D, 0.0D, Block.getStateId(state));
                }
            }
        }
    }
}
