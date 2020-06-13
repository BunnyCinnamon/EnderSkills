package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
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
import org.lwjgl.opengl.GLContext;

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
            Capabilities.get(thePlayer).ifPresent(c -> {
                if (isInactiveNotOwner(thePlayer, c)) {
                    Capabilities.get(event.getEntity()).flatMap(sub -> sub.getActive(ModAbilities.FOG)).ifPresent(holder -> {
                        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                            if (thePlayer != user && user == event.getEntity()) {
                                SkillData data = SkillData.of(holder.data.skill)
                                        .with(holder.data.time - holder.tick)
                                        .put(holder.data.nbt, holder.data.watcher.copy())
                                        .overrides(holder.data.overrides)
                                        .create();
                                PacketHelper.sendFogUseRequestPacket(Minecraft.getMinecraft().player, data);
                            }
                        });
                    });
                }
            });
        }

        @SideOnly(Side.CLIENT)
        public boolean isInactiveNotOwner(EntityPlayerSP thePlayer, SkilledEntityCapability c) {
            return !c.isActive(ModAbilities.FOG) || c.getActives().stream().noneMatch(h -> h.data.skill == ModAbilities.FOG && NBTHelper.getEntity(EntityLivingBase.class, h.data.nbt, "user") != thePlayer);
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
            SkillHelper.getActive(event.getEntity(), ModAbilities.FOG, holder -> {
                Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, holder.data.nbt, "user")).ifPresent(user -> {
                    if (event.getEntity() != user) {
                        float f1 = 2.0F;
                        int i = holder.data.time - holder.tick;
                        if (i < 20) {
                            f1 = f1 + (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 - f1) * (1.0F - (float) i / 20.0F);
                        }
                        GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
                        GlStateManager.setFogStart(f1 * 0.25F);
                        GlStateManager.setFogEnd(f1);
                        if (GLContext.getCapabilities().GL_NV_fog_distance) {
                            GlStateManager.glFogi(34138, 34139);
                        }
                        event.setDensity(1F);
                    } else {
                        GlStateManager.setFog(GlStateManager.FogMode.EXP);
                        event.setDensity(0.02F);
                    }
                    event.setCanceled(true);
                });
            });
        }
    }
}
