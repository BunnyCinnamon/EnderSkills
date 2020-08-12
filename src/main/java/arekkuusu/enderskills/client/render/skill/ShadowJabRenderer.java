package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.model.ModelFist;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.ender.ShadowJab;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ShadowJabRenderer extends SkillRenderer<ShadowJab> {

    public static final ResourceLocation FOLLOWING = new ResourceLocation(LibMod.MOD_ID, "textures/entity/fist.png");
    public static final ModelFist MODEL_FIST = new ModelFist();
    public static final List<Fist> FISTS = Lists.newArrayList();

    public ShadowJabRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onEntityTick(LivingEvent.LivingUpdateEvent event) {
            if (event.getEntityLiving().getEntityWorld().isRemote && ClientProxy.canParticleSpawn()) {
                EntityLivingBase entity = event.getEntityLiving();
                SkillHelper.getActiveFrom(entity, ModAbilities.SHADOW_JAB).ifPresent(data -> {
                    FISTS.add(new Fist(entity, NBTHelper.getDouble(data.nbt, "range")));
                });
            }
        }

        @SubscribeEvent
        public void renderAfterWorld(RenderWorldLastEvent event) {
            float partial = event.getPartialTicks();
            Entity rView = Minecraft.getMinecraft().getRenderViewEntity();
            if (rView == null) rView = Minecraft.getMinecraft().player;
            Entity entity = rView;
            double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partial);
            double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partial);
            double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partial);

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);

            GlStateManager.pushMatrix();
            GlStateManager.translate(-tx, -ty, -tz);
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(FOLLOWING);
            for (Fist fist : FISTS) {
                fist.render(partial);
            }
            GlStateManager.popMatrix();

            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.alphaFunc(516, 0.1F);
            GLHelper.BLEND_SRC_ALPHA$ONE_MINUS_SRC_ALPHA.blend();
            GlStateManager.disableBlend();
        }

        @SubscribeEvent
        public void clientTick(TickEvent.ClientTickEvent event) {
            Iterator<Fist> iterator = FISTS.iterator();
            while (iterator.hasNext()) {
                Fist fist = iterator.next();
                fist.update();
                if (fist.lifeTime++ > fist.maxLifeTime) {
                    iterator.remove();
                }
            }
        }
    }

    public static class Fist {

        public final int maxLifeTime = 5;
        public final WeakReference<Entity> weakReference;
        public float rotationYaw, rotationPitch;
        public Vector position;
        public Vector motion;
        public int lifeTime;

        public Fist(Entity entity, double distance) {
            weakReference = new WeakReference<>(entity);
            position = new Vector(entity.getLookVec().normalize());
            position = position.rotateRandom(entity.world.rand, 40F);

            Vector entityPos = new Vector(entity.getPositionEyes(1F));
            Vector target = entityPos.addVector(
                    position.x * distance,
                    position.y * distance,
                    position.z * distance
            );
            position = position.add(entityPos);
            motion = target.subtract(position);
            motion = new Vector(motion.x / maxLifeTime, motion.y / maxLifeTime, motion.z / maxLifeTime);
            Vector direction = motion.normalize();
            rotationYaw = (float) Math.atan2(direction.x, direction.z) * (float) (180D / Math.PI) - 90F;
            rotationPitch = (float) MathHelper.atan2(direction.y, MathHelper.sqrt(direction.x * direction.x + direction.z * direction.z)) * (float) (180D / Math.PI);
        }

        public void update() {
            //Entity entity = weakReference.get();
            Vector motion = this.motion;
            /*if (entity != null) {
                motion = motion.addVector(entity.motionX, entity.motionY, entity.motionZ);
            }*/
            position = position.add(motion);
        }

        public void render(float partial) {
            GlStateManager.color(0F, 0F, 0F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(position.x, position.y, position.z);
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(FOLLOWING);
            GlStateManager.rotate(rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rotationPitch, 0.0F, 0.0F, 1.0F);
            MODEL_FIST.render(null, 0, 0, 0, 0, 0, 0.0625F);
            GlStateManager.popMatrix();
            GlStateManager.color(1F, 1F, 1F);
        }
    }
}
