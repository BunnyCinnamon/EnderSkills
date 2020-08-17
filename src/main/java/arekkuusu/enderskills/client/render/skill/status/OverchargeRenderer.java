package arekkuusu.enderskills.client.render.skill.status;

import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.effect.Overcharge;
import arekkuusu.enderskills.common.skill.effect.Overheal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class OverchargeRenderer extends SkillRenderer<Overcharge> {

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public OverchargeRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent()
        public void onLivingPostRender(RenderLivingEvent.Post<EntityLivingBase> event) {
            Render<EntityLivingBase> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(event.getEntity());
            if (!(render instanceof RenderLivingBase)) return;
            if (getTick(event.getEntity()) > 0) {
                RenderLivingBase<EntityLivingBase> livingRender = ((RenderLivingBase<EntityLivingBase>) render);
                EntityLivingBase entity = event.getEntity();

                GlStateManager.pushMatrix();
                GlStateManager.translate(event.getX(), event.getY(), event.getZ());
                GlStateManager.enableBlend();
                GlStateManager.depthMask(false);
                GlStateManager.depthFunc(514);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                GlStateManager.pushMatrix();
                GlStateManager.color(0.608F, 0.508F, 0.19F, 1F * (getTick(entity) / 10F));
                Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.scale(0.25F, 0.25F, 0.25F);
                float i = (float) (Minecraft.getSystemTime() % 5000L) / 5000.0F * 6.0F;
                GlStateManager.translate(i, 0.0F, 0.0F);
                GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                renderModel(livingRender, entity, event.getPartialRenderTick());
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.color(0.608F, 0.508F, 0.19F, 1F * (getTick(entity) / 10F));
                Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                float i0 = (float) (Minecraft.getSystemTime() % 6873L) / 6873.0F * 6.0F;
                GlStateManager.translate(-i0, 0.0F, 0.0F);
                GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                renderModel(livingRender, entity, event.getPartialRenderTick());
                GlStateManager.popMatrix();

                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(515);
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                //Rollback
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }
        }

        public void renderModel(RenderLivingBase<EntityLivingBase> render, EntityLivingBase entity, float partialTicks) {
            boolean flag = isVisible(entity);
            boolean flag1 = !flag && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().player);
            if (flag || flag1) {
                boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
                ModelBase base = render.getMainModel();
                float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                float f2 = f1 - f;
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float f8 = handleRotationFloat(entity, partialTicks);
                applyRotations(entity, f, partialTicks);
                float f4 = render.prepareScale(entity, partialTicks);
                float f5 = 0.0F;
                float f6 = 0.0F;

                if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
                    f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    f2 = f1 - f;
                    float f3 = MathHelper.wrapDegrees(f2);

                    if (f3 < -85.0F) {
                        f3 = -85.0F;
                    }

                    if (f3 >= 85.0F) {
                        f3 = 85.0F;
                    }

                    f = f1 - f3;

                    if (f3 * f3 > 2500.0F) {
                        f += f3 * 0.2F;
                    }

                    f2 = f1 - f;
                }

                if (!entity.isRiding()) {
                    f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                    f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                    if (entity.isChild()) {
                        f6 *= 3.0F;
                    }

                    if (f5 > 1.0F) {
                        f5 = 1.0F;
                    }
                    f2 = f1 - f;
                }

                if (flag1) {
                    GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                }
                base.render(entity, f6, f5, f8, f2, f7, f4);
                if (flag1) {
                    GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                }
            }
        }

        public void applyRotations(EntityLivingBase entityLiving, float rotationYaw, float partialTicks) {
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);

            if (entityLiving.deathTime > 0) {
                float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
                f = MathHelper.sqrt(f);

                if (f > 1.0F) {
                    f = 1.0F;
                }

                GlStateManager.rotate(f * 90F, 0.0F, 0.0F, 1.0F);
            } else {
                String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
                if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer) entityLiving).isWearing(EnumPlayerModelParts.CAPE))) {
                    GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                }
            }
        }

        public float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
            float f;

            f = yawOffset - prevYawOffset;
            while (f < -180.0F) {
                f += 360.0F;
            }

            while (f >= 180.0F) {
                f -= 360.0F;
            }

            return prevYawOffset + partialTicks * f;
        }

        public float handleRotationFloat(EntityLivingBase livingBase, float partialTicks) {
            return (float) livingBase.ticksExisted + partialTicks;
        }

        public boolean isVisible(EntityLivingBase livingBase) {
            return !livingBase.isInvisible();
        }

        public void bindTexture(ResourceLocation location) {
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(location);
        }

        @SubscribeEvent
        public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
            if (!event.getEntityLiving().world.isRemote) return;
            boolean active = SkillHelper.isActive(event.getEntity(), ModEffects.OVERCHARGE);
            //Handle tick
            if (active) {
                if (getTick(event.getEntity()) < 10) {
                    setTick(event.getEntity(), getTick(event.getEntity()) + 1);
                }
            } else {
                if (getTick(event.getEntity()) > 0) {
                    setTick(event.getEntity(), getTick(event.getEntity()) - 1);
                }
            }
        }

        public final String tickKey = ModEffects.OVERCHARGE.getRegistryName() + ":tick";

        public int getTick(Entity entity) {
            NBTTagCompound nbt = entity.getEntityData();
            return nbt.hasKey(tickKey) ? nbt.getInteger(tickKey) : -1;
        }

        public void setTick(Entity entity, int tick) {
            NBTTagCompound nbt = entity.getEntityData();
            nbt.setInteger(tickKey, tick);
        }
    }
}
