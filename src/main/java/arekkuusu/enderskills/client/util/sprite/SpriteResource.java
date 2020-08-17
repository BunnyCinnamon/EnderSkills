package arekkuusu.enderskills.client.util.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpriteResource {

	private final ResourceLocation location;

	public SpriteResource(ResourceLocation location) {
		this.location = location;
	}

	public void bind() {
		Minecraft.getMinecraft().renderEngine.bindTexture(getLocation());
	}

	public UVFrame getFrame() {
		return new UVFrame(0, 1, 0, 1);
	}

	public ResourceLocation getLocation() {
		return location;
	}
}
