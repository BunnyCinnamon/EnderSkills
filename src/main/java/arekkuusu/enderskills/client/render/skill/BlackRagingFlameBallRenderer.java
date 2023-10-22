package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityPlaceableDataRenderer;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.offence.blackflame.BlackRagingFlameBall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BlackRagingFlameBallRenderer extends SkillRenderer<BlackRagingFlameBall> {

    public BlackRagingFlameBallRenderer() {
        EntityPlaceableDataRenderer.add(ModAbilities.BLACK_RAGING_FLAME_BALL, Placeable::new);
        EntityThrowableDataRenderer.add(ModAbilities.BLACK_RAGING_FLAME_BALL, Projectile::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableData> {

        protected Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            if (entity.tick % 2 == 0) {
                Vec3d vec = entity.getPositionVector();
                if (entity.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
                    double posX = vec.x + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double posY = vec.y - 0.5 * entity.world.rand.nextDouble();
                    double posZ = vec.z + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    entity.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, (1D - 2D * entity.world.rand.nextDouble()) * 0.05, 0.3 + 0.2 * entity.world.rand.nextDouble(), (1D - 2D * entity.world.rand.nextDouble()) * 0.05);
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(motionX, 0.8 + 0.4 * entity.world.rand.nextDouble(), motionZ), 4F, 25, ResourceLibrary.GLOW_NEGATIVE,  0xFFFFFF);
                }
            }

            if (entity.tick > EntityPlaceableData.MIN_TIME * 2) return;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            float scale = 1F;
            int growTime = EntityPlaceableData.MIN_TIME;
            int shrinkTime = EntityPlaceableData.MIN_TIME;
            if (entity.tick < growTime) {
                scale = (float) entity.tick / (float) growTime;
            } else if (entity.tick > shrinkTime) {
                scale = 1F - (float) (entity.tick - shrinkTime) / 10F;
            }
            this.bindTexture(getEntityTexture(entity));
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 1F, 1F, 1F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime() * 35F);
                    ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime() * 35F);
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
                }
            }
            GL11.glEnable(3042);
            if (entity.tick > growTime && entity.tick < shrinkTime) {
                float ticks = RenderMisc.getRenderPlayerTime() * 40F;
                float stab = 0.25F;
                scale += Math.sin(ticks) * 0.10000000149011612F * stab;
            }
            for (int q = 0; q <= 3; ++q) {
                GlStateManager.pushMatrix();
                float s = entity.getRadius() * ((q < 3) ? (1.1F + 0.05F * q) : 1F) * scale;
                GlStateManager.scale(s, s, s);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 360F, 0F, 1F, 0F);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 720F, 1F, 0F, 0F);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 360F, 0F, 0F, 1F);
                if (q < 3) {
                    GlStateManager.depthMask(false);
                }
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderMisc.drawCubeRaw();
                if (q < 3) {
                    GlStateManager.depthMask(true);
                }
                GlStateManager.popMatrix();
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            GlStateManager.popMatrix();
        }

        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                return ResourceLibrary.DARK_BACKGROUND;
            } else {
                return ResourceLibrary.PORTAL_BACKGROUND;
            }
        }
    }

    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            float scale = 1F;
            this.bindTexture(getEntityTexture(entity));
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.begin();
                    ShaderLibrary.UNIVERSE.set("dimensions", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ShaderLibrary.UNIVERSE.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE.set("color", 1F, 1F, 1F);
                    ShaderLibrary.UNIVERSE.set("ticks", RenderMisc.getRenderPlayerTime() * 35F);
                    ShaderLibrary.UNIVERSE.set("alpha", 0.9F);
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.begin();
                    ShaderLibrary.UNIVERSE_DEFAULT.set("yaw", (Minecraft.getMinecraft().player.rotationYaw * 2F * 3.141592653589793F / 360F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("pitch", -(Minecraft.getMinecraft().player.rotationPitch * 2F * 3.141592653589793F / 360.0F));
                    ShaderLibrary.UNIVERSE_DEFAULT.set("time", RenderMisc.getRenderPlayerTime() * 35F);
                    ShaderLibrary.UNIVERSE_DEFAULT.set("alpha", 0.9F);
                }
            }
            GL11.glEnable(3042);
            for (int q = 0; q <= 3; ++q) {
                GlStateManager.pushMatrix();
                float s = 1 * ((q < 3) ? (1.1F + 0.05F * q) : 1F) * scale;
                GlStateManager.scale(s, s, s);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 360F, 0F, 1F, 0F);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 720F, 1F, 0F, 0F);
                GlStateManager.rotate((entity.ticksExisted * q * 2) % 360F, 0F, 0F, 1F);
                if (q < 3) {
                    GlStateManager.depthMask(false);
                }
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, (q < 3) ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderMisc.drawCubeRaw();
                if (q < 3) {
                    GlStateManager.depthMask(true);
                }
                GlStateManager.popMatrix();
            }
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3042);
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying && !ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                if (!ClientConfig.RENDER_CONFIG.rendering.vanilla) {
                    ShaderLibrary.UNIVERSE.end();
                } else {
                    ShaderLibrary.UNIVERSE_DEFAULT.end();
                }
            }
            GlStateManager.popMatrix();
        }

        @Override
        @Nullable
        protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
            if (!ClientConfig.RENDER_CONFIG.rendering.vanilla || ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                return ResourceLibrary.DARK_BACKGROUND;
            } else {
                return ResourceLibrary.PORTAL_BACKGROUND;
            }
        }
    }
}
