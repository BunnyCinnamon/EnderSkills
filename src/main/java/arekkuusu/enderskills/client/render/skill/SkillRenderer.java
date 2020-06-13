package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public abstract class SkillRenderer<T extends Skill> {

    public final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
    public final Random rand = new Random();
    public SkillRendererDispatcher dispatcher;

    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        //For Rent
    }

    public static float getBlend(int tick, int maxTick, float blend) {
        float startBlend = maxTick * 0.8F;
        float endBlend = maxTick * 0.2F;
        return tick < startBlend ? blend : blend * (1F - ((tick - startBlend) / endBlend));
    }

    public void bindTexture(ResourceLocation location) {
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(location);
    }

    public void setDispatcher(SkillRendererDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
