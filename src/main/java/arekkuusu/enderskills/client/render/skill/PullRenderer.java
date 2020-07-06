package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.wind.Pull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PullRenderer extends SkillRenderer<Pull> {

    public PullRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.PULL, ProjectileWind::new);
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            SkillHelper.getActiveNotOwner(event.getEntity(), ModAbilities.PULL, holder -> {
                if (holder.tick > 10) {
                    ShaderLibrary.GRAY_SCALE.begin();
                }
            });
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            SkillHelper.getActiveNotOwner(event.getEntity(), ModAbilities.PULL, holder -> {
                if (holder.tick > 10) {
                    ShaderLibrary.GRAY_SCALE.end();
                }
            });
        }
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick < 10 && skillHolder.tick % 2 == 0 && entity.world.rand.nextDouble() < 0.4D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;

            Vec3d vector = NBTHelper.getVector(skillHolder.data.nbt, "vector");
            double distance = NBTHelper.getDouble(skillHolder.data.nbt, "force");
            Vec3d from = new Vec3d(posX, posY, posZ);
            Vec3d to = from.addVector(
                    vector.x * distance,
                    vector.y * distance,
                    vector.z * distance
            );
            Vec3d difference = to.subtract(from);
            Vec3d motion = new Vec3d(difference.x / 5D, difference.y / 5D, difference.z / 5D).scale(-1);
            ParticleVanilla vanilla = new ParticleVanilla(entity.world, from, motion, 5F, 18, 0xFFFFFF, 0);
            Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
        }
    }
}
