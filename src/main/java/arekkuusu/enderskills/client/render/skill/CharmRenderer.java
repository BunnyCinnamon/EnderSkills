package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.defense.light.Charm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class CharmRenderer extends SkillRenderer<Charm> {

    private static final ResourceLocation SHADER = new ResourceLocation(LibMod.MOD_ID, "shaders/post/charm.json");
    private static final ResourceLocation PROJECTILE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/charm.png");

    public CharmRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
        EntityThrowableDataRenderer.add(ModAbilities.CHARM, Projectile::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 2 == 0 && entity.world.rand.nextDouble() < 0.2D) {
            Vec3d vec = entity.getPositionEyes(1F);
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() - 0.5D;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 1, 50, entity.world.rand.nextBoolean() ? 0xFFFFFF : 0x58DB11, ResourceLibrary.SPIRAL);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {
        public boolean wasActive = false;

        @SubscribeEvent
        @SuppressWarnings("ConstantConditions")
        public void playerTick(TickEvent.ClientTickEvent event) {
            if (event.type == TickEvent.Type.CLIENT) {
                EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
                if (SkillHelper.isActiveNotOwner(Minecraft.getMinecraft().player, ModAbilities.CHARM)) {
                    if (!wasActive) {
                        renderer.loadShader(SHADER);
                        wasActive = true;
                    }
                } else if (wasActive) {
                    if (renderer.getShaderGroup() != null && renderer.getShaderGroup().getShaderGroupName() != null && SHADER.toString().equals(renderer.getShaderGroup().getShaderGroupName())) {
                        renderer.stopUseShader();
                    }
                    wasActive = false;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Projectile extends Render<EntityThrowableData> {

        protected Projectile(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityThrowableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            for (int i = 0; i < 6; i++) {
                if (entity.world.rand.nextDouble() < 0.2D) {
                    Vec3d vec = entity.getPositionEyes(1F);
                    Vec3d motion = new Vec3d(entity.prevPosX, entity.prevPosY + entity.getEyeHeight(), entity.prevPosZ).subtract(vec);
                    double offset = entity.world.rand.nextDouble();
                    double posX = vec.x + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.x * offset;
                    double posY = vec.y + (entity.height / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.y * offset;
                    double posZ = vec.z + (entity.width / 2) * (entity.world.rand.nextDouble() - 0.5) + motion.z * offset;
                    motion = new Vec3d(0, 0, 0);
                    ES.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), motion, 0.5F, 25, 0xFFFFFF, ResourceLibrary.SPIRAL);
                }
            }
            GlStateManager.pushMatrix();
            GLHelper.BLEND_SRC_ALPHA$ONE.blend();
            ShaderLibrary.BRIGHT.begin();
            ShaderLibrary.BRIGHT.set("alpha", 0.8F);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.bindTexture(getEntityTexture(entity));
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-0.5D, -0.5D, 0).tex(0, 0).endVertex();
            buffer.pos(0.5D, -0.5D, 0).tex(1, 0).endVertex();
            buffer.pos(0.5D, 0.5D, 0).tex(1, 1).endVertex();
            buffer.pos(-0.5D, 0.5D, 0).tex(0, 1).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            ShaderLibrary.BRIGHT.end();
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityThrowableData entity) {
            return PROJECTILE;
        }
    }
}
