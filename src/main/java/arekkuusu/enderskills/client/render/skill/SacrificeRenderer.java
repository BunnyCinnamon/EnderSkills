package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blood.Sacrifice;
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
public class SacrificeRenderer extends SkillRenderer<Sacrifice> {

    public SacrificeRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 0) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x;
            double posY = vec.y + entity.height / 2;
            double posZ = vec.z;
            ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.05, 0), 20F, 50, 0x8A0303, ResourceLibrary.CROSS);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            Capabilities.get(event.getEntity()).flatMap(c -> c.getActive(ModAbilities.SACRIFICE)).ifPresent(holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (event.getEntity() == user) {
                        ShaderLibrary.BLEED.begin();
                        ShaderLibrary.BLEED.set("intensity", 0.8F);
                    }
                });
            });
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            Capabilities.get(event.getEntity()).flatMap(c -> c.getActive(ModAbilities.SACRIFICE)).ifPresent(holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (event.getEntity() == user) {
                        ShaderLibrary.BLEED.end();
                    }
                });
            });
        }
    }
}
