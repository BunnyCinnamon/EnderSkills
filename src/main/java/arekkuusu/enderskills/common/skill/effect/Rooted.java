package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class Rooted extends BaseEffect {

    public Rooted() {
        super(LibNames.ROOTED, new Properties());
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity) && !(entity instanceof EntityPlayer)) return;
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).getNavigator().clearPath();
        }
        entity.moveStrafing = 0;
        entity.moveForward = 0;
        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;
    }

    public void set(EntityLivingBase entity, SkillData data) {
        SkillData status = SkillData.of(this)
                .with(1)
                .put(data.nbt.copy(), data.watcher.copy())
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
