package arekkuusu.enderskills.client.render.effect;

import arekkuusu.enderskills.api.util.Quat;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class Lightning {

    public List<LightningSegment> segments = Lists.newArrayList();
    public final Random rand = new Random();
    public final int generations;
    public final boolean branch;
    public final int age;
    public TextureAtlasSprite particleTexture;
    public float particleRed;
    public float particleGreen;
    public float particleBlue;
    public float offset;
    public int ticks;

    public Lightning(Vector from, Vector to, int generations, float offset, int age, int rgb, boolean branch) {
        this.segments.add(new LightningSegment(from, to));
        this.generations = generations;
        this.branch = branch;
        this.offset = offset;
        this.age = age;
        float r = (rgb >>> 16 & 0xFF) / 256.0F;
        float g = (rgb >>> 8 & 0xFF) / 256.0F;
        float b = (rgb & 0xFF) / 256.0F;
        setRBGColorF(r, g, b);
        setParticleTexture(ResourceLibrary.VOLT_PARTICLE);
    }

    public void make() {
        List<LightningSegment> branched = Lists.newArrayList();
        for (int i = 0; i < generations; i++) {
            List<LightningSegment> temp = Lists.newArrayList();
            for (LightningSegment segment : segments) {
                Vector from = segment.from;
                Vector to = segment.to;
                Vector mid = average(from, to);
                Vector midOffset = to.subtract(from);
                mid = mid.add(midOffset.normalize()
                        .cross(Vector.ONE)
                        .multiply(Vector.fromSpherical(rand.nextFloat() * 360F, rand.nextFloat() * 180F - 90F).multiply(offset))
                );
                if (branch && i < 2) {
                    Vector direction = mid.subtract(from);
                    float xAngle = (20.0F + 10.0F * rand.nextFloat()) * (rand.nextBoolean() ? 1 : -1);
                    float zAngle = (20.0F + 10.0F * rand.nextFloat()) * (rand.nextBoolean() ? 1 : -1);
                    Quat x = Quat.fromAxisAngleRad(Vector.Forward, (float) Math.toRadians(xAngle));
                    Quat z = Quat.fromAxisAngleRad(Vector.Right, (float) Math.toRadians(zAngle));
                    Vector splitEnd = direction
                            .rotate(x.multiply(z))
                            .multiply(0.8D)
                            .add(mid);
                    LightningSegment sub = new LightningSegment(mid.copy(), splitEnd);
                    temp.add(sub);
                }
                LightningSegment one = new LightningSegment(from, mid.copy());
                LightningSegment two = new LightningSegment(mid.copy(), to);
                temp.add(one);
                temp.add(two);
                if (branched.isEmpty() || branched.contains(segment)) {
                    branched.add(two);
                }
            }
            segments = temp;
            offset /= 2;
        }
    }

    private Vector average(Vector one, Vector two) {
        return one.add(two).divide(2D);
    }

    public void onUpdate() {
        ++ticks;
    }

    public boolean isAlive() {
        return ticks < age;
    }

    public void renderParticle(BufferBuilder buffer, float partialTicks) {
        for (LightningSegment s : segments) {
            s.render(buffer, partialTicks);
        }
    }

    public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
        this.particleRed = particleRedIn;
        this.particleGreen = particleGreenIn;
        this.particleBlue = particleBlueIn;
    }

    public void setParticleTexture(ResourceLocation texture) {
        this.particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
    }

    private class LightningSegment {

        private final Vector from;
        private final Vector to;

        LightningSegment(Vector from, Vector to) {
            this.from = from;
            this.to = to;
        }

        void render(BufferBuilder buff, float partialTicks) {
            renderCurrentTextureAroundAxis(buff, from, to, 0F, partialTicks);
            renderCurrentTextureAroundAxis(buff, from, to, 90F, partialTicks);
        }

        private void renderCurrentTextureAroundAxis(BufferBuilder buf, Vector from, Vector to, double angle, float partialTicks) {
            Vector distance = to.subtract(from);
            from = from.offset(distance, -0.1);
            to = to.offset(distance, 0.1);
            Vector direction = to.subtract(from).normalize();
            Vector perpendicular = direction.cross(Vector.ONE).normalize();
            Vector rotatedPerp = perpendicular.rotate(Quat.fromAxisAngleRad(direction, (float) Math.toRadians(angle))).normalize();
            Vector perpFrom = rotatedPerp.multiply(0.025);
            Vector perpTo = rotatedPerp.multiply(0.025);

            double uMin = 0F;
            double uMax = 1F;
            double vMin = 0F;
            double vMax = 1F;
            int light = 255;
            if (particleTexture != null) {
                uMin = particleTexture.getMinU();
                uMax = particleTexture.getMaxU();
                vMin = particleTexture.getMinV();
                vMax = particleTexture.getMaxV();
            }

            Vector vec = from.add(perpFrom.multiply(-1));
            buf.pos(vec.x, vec.y, vec.z).tex(uMax, vMax).color(particleRed, particleGreen, particleBlue, 1).lightmap(light, light).endVertex();
            vec = from.add(perpFrom);
            buf.pos(vec.x, vec.y, vec.z).tex(uMax, vMin).color(particleRed, particleGreen, particleBlue, 1).lightmap(light, light).endVertex();
            vec = to.add(perpTo);
            buf.pos(vec.x, vec.y, vec.z).tex(uMin, vMin).color(particleRed, particleGreen, particleBlue, 1).lightmap(light, light).endVertex();
            vec = to.add(perpTo.multiply(-1));
            buf.pos(vec.x, vec.y, vec.z).tex(uMin, vMax).color(particleRed, particleGreen, particleBlue, 1).lightmap(light, light).endVertex();
        }
    }
}
