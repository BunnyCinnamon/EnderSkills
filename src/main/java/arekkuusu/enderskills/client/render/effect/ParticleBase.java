package arekkuusu.enderskills.client.render.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ParticleBase extends Particle {

    final float initScale;

    public ParticleBase(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        super(world, pos.x, pos.y, pos.z);
        this.motionX = speed.x;
        this.motionY = speed.y;
        this.motionZ = speed.z;
        this.particleAngle = 0/*rand.nextBoolean() ? 2F : -2F * (float) Math.PI*/;
        this.particleMaxAge = age;
        this.particleScale = scale;
        this.initScale = particleScale;
        this.canCollide = false;
        float r = (rgb >>> 16 & 0xFF) / 256.0F;
        float g = (rgb >>> 8 & 0xFF) / 256.0F;
        float b = (rgb & 0xFF) / 256.0F;
        setRBGColorF(r, g, b);
        setParticleTexture(location);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (rand.nextInt(2) == 0) {
            if (this.particleAge++ >= this.particleMaxAge) {
                this.setExpired();
                return;
            }
        }
        float life = (float) particleAge / (float) particleMaxAge;
        this.particleScale = initScale - initScale * life;
        this.particleAlpha = 1.0f - life;
        if (this.particleAngle != 0.0F) {
            this.prevParticleAngle = particleAngle;
            this.particleAngle += 1.0F;
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        //Texture UV
        double uMin = 0F;
        double uMax = 1F;
        double vMin = 0F;
        double vMax = 1F;
        if (this.particleTexture != null) {
            uMin = this.particleTexture.getMinU();
            uMax = this.particleTexture.getMaxU();
            vMin = this.particleTexture.getMinV();
            vMax = this.particleTexture.getMaxV();
        } else {
            uMin = (float)this.particleTextureIndexX / 16.0F;
            uMax = uMin + 0.0624375F;
            vMin = (float)this.particleTextureIndexY / 16.0F;
            vMax = vMin + 0.0624375F;
        }
        //Fix for particle wobbliness
        int i = getBrightnessForRender(partialTicks);
        int j = isAdditive() ? i : i >> 16 & 65535;
        int k = isAdditive() ? i : i & 65535;
        float scale = 0.1F * particleScale;
        double x = prevPosX + (posX - prevPosX) * partialTicks - interpPosX;
        double y = prevPosY + (posY - prevPosY) * partialTicks - interpPosY;
        double z = prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ;
        Vec3d vec0 = new Vec3d(-rotationX * scale - rotationXY * scale, -rotationZ * scale, -rotationYZ * scale - rotationXZ * scale);
        Vec3d vec1 = new Vec3d(-rotationX * scale + rotationXY * scale, rotationZ * scale, -rotationYZ * scale + rotationXZ * scale);
        Vec3d vec2 = new Vec3d(rotationX * scale + rotationXY * scale, rotationZ * scale, rotationYZ * scale + rotationXZ * scale);
        Vec3d vec3 = new Vec3d(rotationX * scale - rotationXY * scale, -rotationZ * scale, rotationYZ * scale - rotationXZ * scale);
        if (this.particleAngle != 0.0F) {
            float angle = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            //Press F to pay respect
            float f = MathHelper.cos(angle * 0.5F);
            float ff = MathHelper.sin(angle * 0.5F) * (float) cameraViewDir.x;
            float fff = MathHelper.sin(angle * 0.5F) * (float) cameraViewDir.y;
            float ffff = MathHelper.sin(angle * 0.5F) * (float) cameraViewDir.z;
            Vec3d vec = new Vec3d((double) ff, (double) fff, (double) ffff);
            vec0 = vec.scale(2.0D * vec0.dotProduct(vec)).add(vec0.scale((f * f) - vec.dotProduct(vec))).add(vec.crossProduct(vec0).scale(2.0F * f));
            vec1 = vec.scale(2.0D * vec1.dotProduct(vec)).add(vec1.scale((f * f) - vec.dotProduct(vec))).add(vec.crossProduct(vec1).scale(2.0F * f));
            vec2 = vec.scale(2.0D * vec2.dotProduct(vec)).add(vec2.scale((f * f) - vec.dotProduct(vec))).add(vec.crossProduct(vec2).scale(2.0F * f));
            vec3 = vec.scale(2.0D * vec3.dotProduct(vec)).add(vec3.scale((f * f) - vec.dotProduct(vec))).add(vec.crossProduct(vec3).scale(2.0F * f));
        }
        buffer.pos(x + vec0.x, y + vec0.y, z + vec0.z).tex(uMax, vMax).color(getRedColorF(), getGreenColorF(), getBlueColorF(), particleAlpha).lightmap(j, k)/*.normal(0.0F, 1.0F, 0.0F)*/.endVertex();
        buffer.pos(x + vec1.x, y + vec1.y, z + vec1.z).tex(uMax, vMin).color(getRedColorF(), getGreenColorF(), getBlueColorF(), particleAlpha).lightmap(j, k)/*.normal(0.0F, 1.0F, 0.0F)*/.endVertex();
        buffer.pos(x + vec2.x, y + vec2.y, z + vec2.z).tex(uMin, vMin).color(getRedColorF(), getGreenColorF(), getBlueColorF(), particleAlpha).lightmap(j, k)/*.normal(0.0F, 1.0F, 0.0F)*/.endVertex();
        buffer.pos(x + vec3.x, y + vec3.y, z + vec3.z).tex(uMin, vMax).color(getRedColorF(), getGreenColorF(), getBlueColorF(), particleAlpha).lightmap(j, k)/*.normal(0.0F, 1.0F, 0.0F)*/.endVertex();
    }

    //Don't mind
    public void setParticleTexture(@Nullable ResourceLocation texture) {
        if(texture != null) {
            setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
        }
    }

    @Override
    public void setParticleTexture(@Nullable TextureAtlasSprite texture) {
        this.particleTexture = texture;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    public boolean isAdditive() {
        return true;
    }

    @Override
    public int getBrightnessForRender(float idk) {
        return isAdditive() ? 255 : super.getBrightnessForRender(idk);
    }

    public void setCanCollide(boolean canCollide) {
        this.canCollide = canCollide;
    }
}
