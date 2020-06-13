package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.common.entity.EntityWall;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class EntityWallRender extends Render<EntityWall> {

    public EntityWallRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityWall entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityWall entity) {
        return null;
    }
}
