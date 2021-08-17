package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.SkillHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ConcurrentModificationException;

public class Slowed extends BaseEffect {

    public static final DynamicModifier SLOW_ATTRIBUTE = new DynamicModifier(
            "9b5b0b97-d8a6-4a46-aaf6-d937e75ce5a4",
            LibMod.MOD_ID + ":" + LibNames.SLOWED,
            SharedMonsterAttributes.MOVEMENT_SPEED,
            Constants.AttributeModifierOperation.MULTIPLY
    );

    public Slowed() {
        super(LibNames.SLOWED, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!isClientWorld(entity) || (entity instanceof EntityPlayer)) {
            if (SkillHelper.isActive(entity, this)) {
                Capabilities.get(entity).ifPresent(c -> {
                    double maxSlow = 1D;
                    try {
                        for (SkillHolder h : c.getActives()) {
                            if (h.data.skill == this) {
                                double slow = h.data.nbt.getDouble("slow");
                                if (slow < maxSlow) maxSlow = slow;
                            }
                        }
                    } catch (ConcurrentModificationException ignored) {
                    }
                    SLOW_ATTRIBUTE.apply(entity, -maxSlow);
                });
            } else {
                SLOW_ATTRIBUTE.remove(entity);
            }
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        this.set(entity, data, 0D);
    }

    public void set(EntityLivingBase entity, SkillData data, double slow) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setDouble(compound, "slow", slow);
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(10)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
