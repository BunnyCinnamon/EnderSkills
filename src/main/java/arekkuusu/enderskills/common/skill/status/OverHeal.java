package arekkuusu.enderskills.common.skill.status;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModStatus;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.concurrent.Executors;

public class OverHeal extends BaseStatus {

    public OverHeal() {
        super(LibNames.OVER_HEAL, new Properties());
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        float overHeal = data.nbt.getFloat("over_heal");
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + overHeal);
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        float overHeal = data.nbt.getFloat("over_heal");
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - overHeal);
    }

    public static void heal(EntityLivingBase entity, float amount) {
        float maxHeal = entity.getMaxHealth();
        float health = entity.getHealth();
        float remainingHeal = (amount + health) - maxHeal;
        entity.heal(amount);
        if (remainingHeal > 0) {
            synchronized (Objects.requireNonNull(entity.world.getMinecraftServer()).futureTaskQueue) {
                entity.world.getMinecraftServer().futureTaskQueue.add(ListenableFutureTask.create(Executors.callable(() -> {
                    NBTTagCompound compound = new NBTTagCompound();
                    NBTHelper.setFloat(compound, "over_heal", remainingHeal);
                    SkillData data = SkillData.of(ModStatus.OVER_HEAL).with(5 * 20).put(compound).create();
                    ModStatus.OVER_HEAL.apply(entity, data);
                    ModStatus.OVER_HEAL.sync(entity, data);
                })));
            }
        }
    }
}
