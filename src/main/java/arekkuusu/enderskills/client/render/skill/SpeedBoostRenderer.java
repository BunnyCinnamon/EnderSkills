package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.SpeedBoost;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class SpeedBoostRenderer extends SkillRenderer<SpeedBoost> {

    public static final WeakHashMap<EntityLivingBase, Vec3d> TRAVELED_VECTORS = new WeakHashMap<>();
    public static final List<AfterImage> AFTER_IMAGES = Lists.newArrayList();

    public SpeedBoostRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        public static boolean rendering = false;

        @SubscribeEvent
        public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
            EntityLivingBase entity = event.getEntityLiving();
            if (!entity.world.isRemote) return; //EAT ASS
            if ((entity.motionX != 0 || entity.motionY != 0 || entity.motionZ != 0) && !entity.isInvisible()) {
                Capabilities.get(entity).filter(c -> c.isActive(ModAbilities.SPEED_BOOST) || c.isActive(ModAbilities.DASH)).ifPresent(c -> {
                    if (!TRAVELED_VECTORS.containsKey(entity)) {
                        TRAVELED_VECTORS.put(entity, entity.getPositionVector());
                    }

                    Vec3d vec = TRAVELED_VECTORS.get(entity);
                    if (entity.getDistance(vec.x, vec.y, vec.z) > entity.width / 2) {
                        TRAVELED_VECTORS.put(entity, entity.getPositionVector());
                        AFTER_IMAGES.add(new AfterImage(entity, entity.getPositionVector()));
                    }
                });
            }
        }

        @SubscribeEvent
        public void clientTick(TickEvent.ClientTickEvent event) {
            AFTER_IMAGES.removeIf(afterImage -> afterImage.lifeTime++ > afterImage.maxLifeTime || afterImage.reference.get() == null);
        }

        @SubscribeEvent
        public void onRenderAfterWorld(RenderWorldLastEvent event) {
            if (!AFTER_IMAGES.isEmpty()) {
                rendering = true; //Lock
                GlStateManager.pushAttrib();
                GlStateManager.pushMatrix();
                GlStateManager.depthMask(false);
                GlStateManager.alphaFunc(516, 0.003921569f);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                EntityPlayerSP playerEntity = Minecraft.getMinecraft().player;
                double x = playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * 1F;
                double y = playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * 1F;
                double z = playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * 1F;
                GlStateManager.translate(-x, -y, -z);
                for (AfterImage afterImage : AFTER_IMAGES) {
                    afterImage.render();
                }
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1f);
                GlStateManager.depthMask(true);
                GlStateManager.popMatrix();
                GlStateManager.popAttrib();
                rendering = false; //Unlock
            }
        }
    }

    public static class AfterImage {

        public final WeakReference<EntityLivingBase> reference;
        public final Vec3d positionVector;
        public final int maxLifeTime = 10;
        public int lifeTime;

        public AfterImage(EntityLivingBase entity, Vec3d positionVector) {
            this.reference = new WeakReference<>(entity);
            this.positionVector = positionVector;
        }

        public void render() {
            EntityLivingBase entity = reference.get();
            if (entity == null) return;
            int i = entity.getBrightnessForRender();
            if (entity.isBurning()) {
                i = 15728880;
            }
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

            GlStateManager.pushMatrix();
            GlStateManager.translate(positionVector.x, positionVector.y, positionVector.z);
            GlStateManager.color(1F, 1F, 1F, 0.3F * (1F - ((float) lifeTime / (float) maxLifeTime)));
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, 0, 0, entity.rotationYaw, 0F, false);
            GlStateManager.popMatrix();
        }
    }
}
