package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;
import java.util.Map;

public class EntityPlaceableDataRenderer extends Render<EntityPlaceableData> {

    public final static Map<Skill, IRenderFactory<? super EntityPlaceableData>> DEFERRED_RENDERS = Maps.newHashMap();
    public final static Map<Skill, Render<? super EntityPlaceableData>> RENDERS = Maps.newHashMap();

    public static void init(RenderManager manager){
        DEFERRED_RENDERS.forEach((key, value) -> RENDERS.put(key, value.createRenderFor(manager)));
    }

    public EntityPlaceableDataRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPlaceableData entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if(entity.getData() != null && RENDERS.containsKey(entity.getData().skill)) {
            RENDERS.get(entity.getData().skill).doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPlaceableData entity) {
        return null;
    }

    //Fuck Off
    @Override
    protected void renderEntityName(EntityPlaceableData entityIn, double x, double y, double z, String name, double distanceSq) {

    }

    @Override
    protected void renderLivingLabel(EntityPlaceableData entityIn, String str, double x, double y, double z, int maxDistance) {

    }

    @Override
    protected void renderName(EntityPlaceableData entity, double x, double y, double z) {

    }

    @Override
    public void setRenderOutlines(boolean renderOutlinesIn) {

    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {

    }

    @Override
    public boolean canRenderName(EntityPlaceableData entity) {
        return false;
    }
    //Fuck Off

    public static void add(Skill skill, IRenderFactory<? super EntityPlaceableData> render) {
        DEFERRED_RENDERS.put(skill, render);
    }
}
