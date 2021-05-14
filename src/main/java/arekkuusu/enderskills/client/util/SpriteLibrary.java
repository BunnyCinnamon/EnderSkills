package arekkuusu.enderskills.client.util;

import arekkuusu.enderskills.client.render.entity.VoltaicSentinelRender;
import arekkuusu.enderskills.client.render.skill.HomeStarRenderer;
import arekkuusu.enderskills.client.render.skill.MagneticPullRenderer;
import arekkuusu.enderskills.client.render.skill.ShockingAuraRenderer;
import arekkuusu.enderskills.client.util.sprite.SpriteAtlasResource;
import arekkuusu.enderskills.client.util.sprite.SpriteFrameResource;
import arekkuusu.enderskills.client.util.sprite.SpriteResource;
import arekkuusu.enderskills.common.EnderSkills;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SpriteLibrary {

    public static final SpriteFrameResource VOLTAIC_SENTINEL = load(
            VoltaicSentinelRender.TEXTURE, 9, 1
    );

    public static final SpriteFrameResource MAGNETIC_PULL = load(
            MagneticPullRenderer.TEXTURE, 8, 1
    );

    public static final SpriteFrameResource ELECTRIC_RING = load(
            ShockingAuraRenderer.TEXTURE, 8, 1
    );

    public static SpriteAtlasResource find(ResourceLocation location) {
        SpriteAtlasResource sprite = new SpriteAtlasResource(location);
        ResourceLibrary.ATLAS_SET.add(location);
        return sprite;
    }

    public static SpriteResource load(ResourceLocation location) {
        return new SpriteResource(location);
    }

    public static SpriteFrameResource load(ResourceLocation location, int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            EnderSkills.LOG.fatal("[SpriteLibrary] Your sprite can't have 0 rows or columns" + location.toString());
        }
        return new SpriteFrameResource(location, rows, columns);
    }

    public static void preInit() {
    }
}
