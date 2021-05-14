package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blood.Syphon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SyphonRenderer extends SkillRenderer<Syphon> {

    public SyphonRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.SYPHON, ProjectileBloodRenderer::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 1) {
            Entity source = NBTHelper.getEntity(EntityLivingBase.class, skillHolder.data.nbt, "owner");
            if (source != null) {
                for (int i = 0; i < 6; i++) {
                    if(ClientProxy.canParticleSpawn()) {
                        Vec3d vecFrom = entity.getPositionVector();
                        double posX = vecFrom.x + (entity.width / 2) * (rand.nextDouble() - 0.5);
                        double posY = vecFrom.y + entity.height * rand.nextDouble();
                        double posZ = vecFrom.z + (entity.width / 2) * (rand.nextDouble() - 0.5);
                        Vec3d motion = vecFrom.subtract(source.getPositionVector()).normalize().scale(-1);
                        motion = new Vec3d(motion.x / 5, motion.y / 5, motion.z / 5);
                        EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), motion, 5F, 20, 0x8A0303, ResourceLibrary.MOTE);
                    }
                }
            }
        }
    }
}
