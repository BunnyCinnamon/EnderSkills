package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.ender.Gloom;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import java.lang.ref.WeakReference;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GloomRenderer extends SkillRenderer<Gloom> {

    public static final List<Beam> BEAMS = Lists.newArrayList();

    public GloomRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.GLOOM, ProjectileVoid::new);
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 0) {
            Entity source = NBTHelper.getEntity(EntityLivingBase.class, skillHolder.data.nbt, "user");
            if (source != null) {
                BEAMS.add(new Beam(entity, source));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void renderBeamAfterWorld(RenderWorldLastEvent event) {
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
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.DARK_BACKGROUND);
                } else {
                    Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ResourceLibrary.PORTAL_BACKGROUND);
                }
            }
            for (Beam beam : BEAMS) {
                beam.render(partial);
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
            BEAMS.removeIf(beam -> beam.lifeTime++ > beam.maxLifeTime || beam.referenceTo.get() == null || beam.referenceFrom.get() == null);
        }
    }

    public static class Beam {

        public final WeakReference<Entity> referenceFrom;
        public final WeakReference<Entity> referenceTo;
        public final int maxLifeTime = 20;
        public int lifeTime;

        public Beam(Entity entityFrom, Entity entityTo) {
            this.referenceFrom = new WeakReference<>(entityFrom);
            this.referenceTo = new WeakReference<>(entityTo);
        }

        public void render(float partial) {
            Entity source = referenceFrom.get();
            Entity entity = referenceTo.get();
            if (source != null && entity != null) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                    if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                        ShaderLibrary.UNIVERSE.begin();
                        ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                        ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                        ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                        ShaderLibrary.UNIVERSE.set("color", 0.36F, 0.12F, 0.4F);
                        ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime());
                        ShaderLibrary.UNIVERSE.set("alpha", SkillRenderer.getBlend(lifeTime, maxLifeTime, 0.6F));
                    } else {
                        ShaderLibrary.UNIVERSE_DEFAULT.begin();
                        ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                        ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                        ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime());
                        ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", SkillRenderer.getBlend(lifeTime, maxLifeTime, 0.6F));
                    }
                } else {
                    ShaderLibrary.BRIGHT.begin();
                    ShaderLibrary.BRIGHT.set("alpha", SkillRenderer.getBlend(lifeTime, maxLifeTime, 0.6F));
                }
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                Vec3d from = source.getPositionVector().addVector(0, source.height / 2, 0);
                Vec3d to = entity.getPositionVector().addVector(0, entity.height / 2, 0);
                renderTextureAroundAxis(buffer, from, to, 0F, partial);
                renderTextureAroundAxis(buffer, from, to, 90F * Math.PI / 180F, partial);
                tessellator.draw();
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                    if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                        ShaderLibrary.UNIVERSE.end();
                    } else {
                        ShaderLibrary.UNIVERSE_DEFAULT.end();
                    }
                } else {
                    ShaderLibrary.BRIGHT.end();
                }
            }
        }

        private void renderTextureAroundAxis(BufferBuilder buf, Vec3d from, Vec3d to, double angle, float partialTicks) {
            Vec3d direction = to.subtract(from).normalize();
            Vec3d perpendicular = perpendicular(direction).normalize();
            Quaternion quat = new Quaternion();
            quat.setFromAxisAngle(new Vector4f((float) direction.x, (float) direction.y, (float) direction.z, (float) angle));
            Vec3d rotatedPerp = rotate(perpendicular, quat).normalize();
            Vec3d perpFrom = rotatedPerp.scale(0.25);
            Vec3d perpTo = rotatedPerp.scale(0.25);

            int rgb = 0x1E0034;
            float r = (rgb >>> 16 & 0xFF) / 256.0F;
            float g = (rgb >>> 8 & 0xFF) / 256.0F;
            float b = (rgb & 0xFF) / 256.0F;
            double uMin = 0F;
            double uMax = 1F;
            double vMin = 0F;
            double vMax = 1F;
            Vec3d vec = from.add(perpFrom.scale(-1));
            buf.pos(vec.x, vec.y, vec.z).tex(uMax, vMax).color(r, g, b, 1F).endVertex();
            vec = from.add(perpFrom);
            buf.pos(vec.x, vec.y, vec.z).tex(uMax, vMin).color(r, g, b, 1F).endVertex();
            vec = to.add(perpTo);
            buf.pos(vec.x, vec.y, vec.z).tex(uMin, vMin).color(r, g, b, 1F).endVertex();
            vec = to.add(perpTo.scale(-1));
            buf.pos(vec.x, vec.y, vec.z).tex(uMin, vMax).color(r, g, b, 1F).endVertex();
        }

        public Vec3d rotate(Vec3d vec, Quaternion quaternion) {
            double vx = vec.x;
            double vy = vec.y;
            double vz = vec.z;
            double rx = quaternion.x;
            double ry = quaternion.y;
            double rz = quaternion.z;
            double rw = quaternion.w;

            double tx = 2 * (ry * vz - rz * vy);
            double ty = 2 * (rz * vx - rx * vz);
            double tz = 2 * (rx * vy - ry * vx);

            double cx = ry * tz - rz * ty;
            double cy = rz * tx - rx * tz;
            double cz = rx * ty - ry * tx;

            double newX = vx + rw * tx + cx;
            double newY = vy + rw * ty + cy;
            double newZ = vz + rw * tz + cz;

            return new Vec3d(newX, newY, newZ);
        }

        public Vec3d perpendicular(Vec3d vec) {
            if (vec.z == 0D) {
                return zCrossProduct(vec);
            }
            return xCrossProduct(vec);
        }

        public Vec3d xCrossProduct(Vec3d vec) {
            double d = vec.z;
            double d1 = -vec.y;
            return new Vec3d(0D, d, d1);
        }

        public Vec3d zCrossProduct(Vec3d vec) {
            double d = vec.y;
            double d1 = -vec.x;
            return new Vec3d(d, d1, 0D);
        }
    }
}
