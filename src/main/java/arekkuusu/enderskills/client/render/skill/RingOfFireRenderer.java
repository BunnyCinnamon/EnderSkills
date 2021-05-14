package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableRingOfFire;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ability.defense.fire.RingOfFire;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class RingOfFireRenderer extends SkillRenderer<RingOfFire> {

    public RingOfFireRenderer() {
        EntityThrowableDataRenderer.add(ModAbilities.RING_OF_FIRE, ProjectileFireRenderer::new);
    }

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableRingOfFire> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableRingOfFire entity, double x, double y, double z, float entityYaw, float partialTicks) {
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableRingOfFire entity) {
            return TextureMap.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
