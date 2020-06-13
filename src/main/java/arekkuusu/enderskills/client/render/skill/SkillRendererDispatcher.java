package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.registry.Skill;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class SkillRendererDispatcher {

    public static final SkillRendererDispatcher INSTANCE = new SkillRendererDispatcher();
    public final Map<Class<? extends Skill>, SkillRenderer<? extends Skill>> skillRendererMap = Maps.newHashMap();

    private SkillRendererDispatcher() {
        //Yoink!
    }

    @Nullable
    public <T extends Skill> SkillRenderer<T> getRenderer(Class<? extends Skill> skill) {
        //noinspection unchecked
        SkillRenderer<T> skillRenderer = (SkillRenderer<T>) skillRendererMap.get(skill);
        if (skillRenderer == null && skill != Skill.class) {
            //noinspection unchecked
            skillRenderer = SkillRendererDispatcher.INSTANCE.getRenderer((Class<? extends Skill>) skill.getSuperclass());
            this.skillRendererMap.put(skill, skillRenderer);
        }
        return skillRenderer;
    }

    @Nullable
    public <T extends Skill> SkillRenderer<T> getRenderer(Skill skill) {
        return getRenderer(skill.getClass());
    }

    public <T extends Skill> void registerRenderer(Class<T> skill, SkillRenderer<T> renderer) {
        SkillRendererDispatcher.INSTANCE.skillRendererMap.put(skill, renderer);
        renderer.setDispatcher(SkillRendererDispatcher.INSTANCE);
    }
}
