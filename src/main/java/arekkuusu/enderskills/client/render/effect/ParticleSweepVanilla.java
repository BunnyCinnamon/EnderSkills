package arekkuusu.enderskills.client.render.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleSweepVanilla extends ParticleBase {

    public static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
    public static final ResourceLocation SWEEP_TEXTURE = new ResourceLocation("textures/entity/sweep.png");

    public ParticleSweepVanilla(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb) {
        super(world, pos, speed, scale, age, rgb, null);
        float f = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = f;
        this.particleGreen = f;
        this.particleBlue = f;
    }

    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        int i = (int) (((float) this.particleAge + partialTicks) * 3.0F / (float) this.particleMaxAge);

        if (i <= 7) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SWEEP_TEXTURE);
            float f = (float) (i % 4) / 4.0F;
            float f1 = f + 0.24975F;
            float f2 = (float) (i / 2) / 2.0F;
            float f3 = f2 + 0.4995F;
            float scale = 1.0F * this.particleScale;

            double x = prevPosX + (posX - prevPosX) * partialTicks - interpPosX;
            double y = prevPosY + (posY - prevPosY) * partialTicks - interpPosY;
            double z = prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ;
            Vec3d vec0 = new Vec3d(-rotationX * scale - rotationXY * scale, -rotationZ * scale, -rotationYZ * scale - rotationXZ * scale);
            Vec3d vec1 = new Vec3d(-rotationX * scale + rotationXY * scale, rotationZ * scale, -rotationYZ * scale + rotationXZ * scale);
            Vec3d vec2 = new Vec3d(rotationX * scale + rotationXY * scale, rotationZ * scale, rotationYZ * scale + rotationXZ * scale);
            Vec3d vec3 = new Vec3d(rotationX * scale - rotationXY * scale, -rotationZ * scale, rotationYZ * scale - rotationXZ * scale);
            if (this.particleAngle != 0.0F) {
                float angle = 995F;
                //Press F to pay respect
                float ff = MathHelper.cos(angle);
                float fff = MathHelper.sin(angle) * (float) cameraViewDir.x;
                float ffff = MathHelper.sin(angle) * (float) cameraViewDir.y;
                float fffff = MathHelper.sin(0) * (float) cameraViewDir.z;
                Vec3d vec = new Vec3d(fff, ffff, fffff);
                vec0 = vec.scale(2.0D * vec0.dotProduct(vec)).add(vec0.scale((ff * ff) - vec.dotProduct(vec))).add(vec.crossProduct(vec0).scale(2.0F * ff));
                vec1 = vec.scale(2.0D * vec1.dotProduct(vec)).add(vec1.scale((ff * ff) - vec.dotProduct(vec))).add(vec.crossProduct(vec1).scale(2.0F * ff));
                vec2 = vec.scale(2.0D * vec2.dotProduct(vec)).add(vec2.scale((ff * ff) - vec.dotProduct(vec))).add(vec.crossProduct(vec2).scale(2.0F * ff));
                vec3 = vec.scale(2.0D * vec3.dotProduct(vec)).add(vec3.scale((ff * ff) - vec.dotProduct(vec))).add(vec.crossProduct(vec3).scale(2.0F * ff));
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            buffer.begin(7, VERTEX_FORMAT);
            buffer.pos(x + vec0.x, y + vec0.y, z + vec0.z).tex(f1, f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(x + vec1.x, y + vec1.y, z + vec1.z).tex(f1, f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(x + vec2.x, y + vec2.y, z + vec2.z).tex(f, f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(x + vec3.x, y + vec3.y, z + vec3.z).tex(f, f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.enableLighting();
        }
    }

    @Override
    public boolean isAdditive() {
        return true;
    }

    public int getFXLayer() {
        return 3;
    }

    public int getBrightnessForRender(float p_189214_1_) {
        return 61680;
    }
}
