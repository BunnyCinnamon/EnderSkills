package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Bleeding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BleedingRenderer extends SkillRenderer<Bleeding> {

    public BleedingRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onUpdate(LivingEvent.LivingUpdateEvent event) {
            if(!event.getEntityLiving().world.isRemote) return;
            Entity entity = event.getEntityLiving();
            if (entity.ticksExisted % 5 == 0 && ClientProxy.canParticleSpawn()) {
                if (SkillHelper.isActive(event.getEntityLiving(), ModEffects.BLEEDING)) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                    double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
                    double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                    EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, -0.01, 0), 2F, 50, 0x690303, ResourceLibrary.DROPLET);
                }
            }
        }

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            if (SkillHelper.isActive(event.getEntity(), ModEffects.BLEEDING)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BLEED.begin();
                    ShaderLibrary.BLEED.set("intensity", 0.5F);
                }
            }
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            if (SkillHelper.isActive(event.getEntity(), ModEffects.BLEEDING)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BLEED.end();
                }
            }
        }
    }
}
