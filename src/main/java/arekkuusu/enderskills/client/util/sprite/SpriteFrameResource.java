package arekkuusu.enderskills.client.util.sprite;

import arekkuusu.enderskills.client.util.helper.RenderMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpriteFrameResource extends SpriteResource {

    private final int rows, columns;
    private final double frames;
    private final double u;
    private final double v;

    public SpriteFrameResource(ResourceLocation location, int rows, int columns) {
        super(location);
        this.columns = columns;
        this.rows = rows;
        this.u = 1D / columns;
        this.v = 1D / rows;
        this.frames = rows * columns;
    }

    @Override
    public void bind() {
        Minecraft.getMinecraft().renderEngine.bindTexture(getLocation());
    }

    @Override
    public UVFrame getFrame() {
        return getFrame(RenderMisc.getRenderPlayerTime());
    }

    public UVFrame getFrame(float ticks) {
        int frame = (int) (ticks % frames);
        double u = ((double) frame % (double) columns) * getU();
        double v = ((double) frame / (double) columns) * getV();
        return new UVFrame(u, u + getU(), v, v + getV());
    }

    public double getU() {
        return u;
    }

    public double getV() {
        return v;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public double getFrames() {
        return frames;
    }
}
