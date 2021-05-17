package arekkuusu.enderskills.api.capability;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;
import java.util.Optional;

public final class Capabilities {
    @CapabilityInject(SkilledEntityCapability.class)
    public static final Capability<SkilledEntityCapability> SKILLED_ENTITY = null;
    @CapabilityInject(SkillGroupCapability.class)
    public static final Capability<SkillGroupCapability> WEIGHT = null;
    @CapabilityInject(EnduranceCapability.class)
    public static final Capability<EnduranceCapability> ENDURANCE = null;
    @CapabilityInject(AdvancementCapability.class)
    public static final Capability<AdvancementCapability> ADVANCEMENT = null;
    @CapabilityInject(PowerBoostCapability.class)
    public static final Capability<PowerBoostCapability> POWER_BOOST = null;
    @CapabilityInject(KnockbackTimerCapability.class)
    public static final Capability<KnockbackTimerCapability> KNOCKBACK_TIMER = null;

    public static Optional<SkilledEntityCapability> get(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(SKILLED_ENTITY, null)) : Optional.empty();
    }

    public static Optional<SkillGroupCapability> weight(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(WEIGHT, null)) : Optional.empty();
    }

    public static Optional<EnduranceCapability> endurance(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(ENDURANCE, null)) : Optional.empty();
    }

    public static Optional<AdvancementCapability> advancement(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(ADVANCEMENT, null)) : Optional.empty();
    }

    public static Optional<PowerBoostCapability> powerBoost(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(POWER_BOOST, null)) : Optional.empty();
    }

    public static Optional<KnockbackTimerCapability> knockback(@Nullable Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCapability(KNOCKBACK_TIMER, null)) : Optional.empty();
    }
}
