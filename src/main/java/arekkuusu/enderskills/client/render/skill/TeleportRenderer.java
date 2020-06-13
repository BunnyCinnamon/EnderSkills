package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.Teleport;
import com.google.common.collect.Lists;
import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
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
import org.lwjgl.opengl.GL11;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class TeleportRenderer extends SkillRenderer<Teleport> {

    public static final List<TeleportRift> TELEPORT_RIFTS = Lists.newArrayList();

    public TeleportRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void entityTick(LivingEvent.LivingUpdateEvent event) {
            if(!event.getEntity().world.isRemote) return;
            Capabilities.get(event.getEntityLiving()).flatMap(c -> c.getActive(ModAbilities.TELEPORT).filter(h -> h.tick == 0)).ifPresent(h -> {
                Vec3d offset = new Vec3d(0, event.getEntity().height / 2D, 0);
                TeleportRift riftOrigin = new TeleportRift(event.getEntityLiving(), NBTHelper.getVector(h.data.nbt, "origin").add(offset));
                TeleportRift riftTarget = new TeleportRift(event.getEntityLiving(), NBTHelper.getVector(h.data.nbt, "target").add(offset));
                TELEPORT_RIFTS.add(riftOrigin);
                TELEPORT_RIFTS.add(riftTarget);
            });
        }

        @SubscribeEvent
        public void clientTick(TickEvent.ClientTickEvent event) {
            TELEPORT_RIFTS.removeIf(teleportRift -> teleportRift.lifeTime++ > teleportRift.maxLifeTime || teleportRift.reference.get() == null);
        }

        @SubscribeEvent
        public void onRenderAfterWorld(RenderWorldLastEvent event) {
            if (!TELEPORT_RIFTS.isEmpty()) {
                GlStateManager.pushMatrix();
                EntityPlayerSP playerEntity = Minecraft.getMinecraft().player;
                double x = playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * 1F;
                double y = playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * 1F;
                double z = playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * 1F;
                GlStateManager.translate(-x, -y, -z);
                for (TeleportRift teleportRift : TELEPORT_RIFTS) {
                    teleportRift.render(event.getPartialTicks());
                }
                GlStateManager.popMatrix();
            }
        }
    }

    public static class TeleportRift {

        public ArrayList<Float> pointsWidth = Lists.newArrayList();
        public ArrayList<Vec3d> points = Lists.newArrayList();
        public final WeakReference<EntityLivingBase> reference;
        public final Vec3d positionVector;
        public final int maxLifeTime = 20;
        public final Random random;
        public final CoreGLE gle;
        public final float width;
        public final float height;
        public int lifeTime;

        public TeleportRift(EntityLivingBase entity, Vec3d positionVector) {
            this.reference = new WeakReference<>(entity);
            this.positionVector = positionVector;
            this.random = new Random(entity.world.rand.nextLong());
            this.gle = new CoreGLE();
            this.setupShape(entity);
            this.width = entity.width;
            this.height = entity.height;
        }

        public void setupShape(Entity entity) {
            points.clear();
            pointsWidth.clear();
            int steps = 3;
            float girth = entity.width;
            double angle = 0.35;
            Vec3d right = new Vec3d(0, entity.height / (steps + 1), 0);
            Vec3d left = right.scale(-1);
            Vec3d lr = new Vec3d(0, 0, 0);
            Vec3d ll = new Vec3d(0, 0, 0);
            float dec = girth / steps;
            for (int a = 0; a < steps; ++a) {
                girth -= dec;
                right = right.rotatePitch((float) (random.nextGaussian() * angle));
                right = right.rotateYaw((float) (random.nextGaussian() * angle));
                lr = lr.add(right);
                points.add(new Vec3d(lr.x, lr.y, lr.z));
                pointsWidth.add(girth);
                left = left.rotatePitch((float) (random.nextGaussian() * angle));
                left = left.rotateYaw((float) (random.nextGaussian() * angle));
                ll = ll.add(left);
                points.add(0, new Vec3d(ll.x, ll.y, ll.z));
                pointsWidth.add(0, girth);
            }
            lr = lr.add(right);
            points.add(new Vec3d(lr.x, lr.y, lr.z));
            pointsWidth.add(0.0F);
            ll = ll.add(left);
            points.add(0, new Vec3d(ll.x, ll.y, ll.z));
            pointsWidth.add(0, 0F);
        }

        public void render(float partialTicks) {
            EntityLivingBase entity = reference.get();
            if (entity == null) return;
            GlStateManager.pushMatrix();
            GlStateManager.translate(positionVector.x, positionVector.y, positionVector.z);
            if (!CommonConfig.RENDER_CONFIG.rendering.vanilla || CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.DARK_BACKGROUND);
            } else {
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
            }
            if(!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if(!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
                }
            }
            float scale = 1F;
            int growTime = 10;
            int shrinkTime = maxLifeTime - 10;
            if (lifeTime < growTime) {
                scale = (float) lifeTime / (float) growTime;
            } else if (lifeTime > shrinkTime) {
                scale = 1F - (float) (lifeTime - shrinkTime) / 10F;
            }
            float amp = 1.0F;
            float stab = 1F - 4 / 50F;
            GL11.glEnable(3042);
            for (int q = 0; q <= 3; ++q) {
                if (q < 3) {
                    GlStateManager.depthMask(false);
                }
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
                if (points.size() > 2) {
                    GL11.glPushMatrix();
                    double[][] pp = new double[points.size()][3];
                    float[][] colours = new float[points.size()][4];
                    double[] radii = new double[points.size()];
                    for (int a = 0; a < points.size(); ++a) {
                        float tick = entity.ticksExisted + partialTicks + q;
                        if (a > points.size() / 2) {
                            tick -= a * 10;
                        } else if (a < points.size() / 2) {
                            tick += a * 10;
                        }
                        pp[a][0] = points.get(a).x;
                        pp[a][1] = points.get(a).y;
                        pp[a][2] = points.get(a).z;
                        colours[a][0] = 1F;
                        colours[a][1] = 1F;
                        colours[a][2] = 1F;
                        colours[a][3] = 1F;
                        final double w = 1.0 - Math.sin(tick / 8F * amp) * 0.10000000149011612F * stab;
                        radii[a] = pointsWidth.get(a) * w * ((q < 3) ? (1.1F + 0.15F * q) : 1F) * scale;
                    }
                    this.gle.set_POLYCYL_TESS(6);
                    this.gle.gleSetJoinStyle(1026);
                    this.gle.glePolyCone(pp.length, pp, colours, radii, 1F, 0F);
                    GL11.glPopMatrix();
                }
                if (q < 3) {
                    GlStateManager.depthMask(true);
                }
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if(!CommonConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if(!CommonConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            GlStateManager.popMatrix();
        }
    }
}
