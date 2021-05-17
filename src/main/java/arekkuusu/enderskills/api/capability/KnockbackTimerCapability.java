package arekkuusu.enderskills.api.capability;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class KnockbackTimerCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<KnockbackTimerCapability> {

    public double lastKnockback;

    public static void init() {
        CapabilityManager.INSTANCE.register(KnockbackTimerCapability.class, new KnockbackTimerCapability(), KnockbackTimerCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.KNOCKBACK_TIMER ? Capabilities.KNOCKBACK_TIMER.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.KNOCKBACK_TIMER.getStorage().writeNBT(Capabilities.KNOCKBACK_TIMER, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.KNOCKBACK_TIMER.getStorage().readNBT(Capabilities.KNOCKBACK_TIMER, this, null, nbt);
    }

    //** NBT **//
    public static final String KNOCKBACK_NBT = "lastKnockback";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<KnockbackTimerCapability> capability, KnockbackTimerCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        //Write Endurance
        tag.setDouble(KNOCKBACK_NBT, instance.lastKnockback);
        return tag;
    }

    @Override
    public void readNBT(Capability<KnockbackTimerCapability> capability, KnockbackTimerCapability instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        //Read endurance
        instance.lastKnockback = tag.getDouble(KNOCKBACK_NBT);
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "KNOCKBACK");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.KNOCKBACK_TIMER.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.KNOCKBACK_TIMER, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.KNOCKBACK_TIMER, null).serializeNBT());
        }
    }
}
