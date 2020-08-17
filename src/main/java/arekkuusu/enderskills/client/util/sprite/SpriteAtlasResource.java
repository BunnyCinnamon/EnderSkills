package arekkuusu.enderskills.client.util.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class SpriteAtlasResource extends SpriteResource {

	public SpriteAtlasResource(ResourceLocation location) {
		super(location);
	}

	@Override
	public void bind() {
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public UVFrame getFrame() {
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getLocation().toString());
		return new UVFrame(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
	}
}
