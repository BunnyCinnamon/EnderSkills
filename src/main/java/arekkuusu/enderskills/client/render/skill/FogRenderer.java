package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.Fog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

@SideOnly(Side.CLIENT)
public class FogRenderer extends SkillRenderer<Fog> {

    public FogRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onLivingRender(RenderLivingEvent.Post<EntityLivingBase> event) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
            if (!SkillHelper.isActive(thePlayer, ModEffects.BLINDED)) {
                SkillHelper.getActiveFrom(event.getEntity(), ModAbilities.FOG).ifPresent(data -> {
                    Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, data.nbt, "owner")).ifPresent(user -> {
                        if (thePlayer != user && user == event.getEntity()) {
                            PacketHelper.sendBlindedUseRequestPacket(Minecraft.getMinecraft().player, data);
                        }
                    });
                });
            }
        }

        @SubscribeEvent
        public void onSoundEffect(PlaySoundAtEntityEvent event) {
            if (event.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) event.getEntity();
                Capabilities.get(entity).flatMap(c -> c.getActive(ModAbilities.FOG)).ifPresent(holder -> {
                    event.setVolume(event.getVolume() * 0.5F);
                });
            }
        }

        @SubscribeEvent
        public void onFogDensityRender(EntityViewRenderEvent.FogDensity event) {
            if (!SkillHelper.isActive(event.getEntity(), ModEffects.BLINDED) && SkillHelper.isActive(event.getEntity(), ModAbilities.FOG)) {
                GlStateManager.setFog(GlStateManager.FogMode.EXP);
                event.setDensity(0.02F);
                event.setCanceled(true);
            }
        }
    }
}
