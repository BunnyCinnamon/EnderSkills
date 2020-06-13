package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.blood.Bleed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

@SideOnly(Side.CLIENT)
public class BleedRenderer extends SkillRenderer<Bleed> {

    public BleedRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 5 == 0 && entity.world.rand.nextDouble() < 0.2D) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, -0.1, 0), 2F, 50, 0x8A0303, ResourceLibrary.DROPLET);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            SkillHelper.getActive(event.getEntity(), ModAbilities.BLEED, holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (event.getEntity() == user) {
                        ShaderLibrary.BLEED.begin();
                        ShaderLibrary.BLEED.set("intensity", 0.2F);
                    }
                });
            });
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            SkillHelper.getActive(event.getEntity(), ModAbilities.BLEED, holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (event.getEntity() == user) {
                        ShaderLibrary.BLEED.end();
                    }
                });
            });
        }
    }
}
