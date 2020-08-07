package arekkuusu.enderskills.common.skill.status;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ModAttributes;

public class BaseStatus extends Skill {

    public BaseStatus(String id, Properties properties) {
        super(properties);
        ModAttributes.setRegistry(this, id);
    }
}
