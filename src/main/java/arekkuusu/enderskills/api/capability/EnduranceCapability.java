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
public class EnduranceCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<EnduranceCapability> {

    private double enduranceDelay;
    private double endurance;
    private double absorption;

    public double getAbsorption() {
        return absorption;
    }

    public void setAbsorption(double absorption) {
        this.absorption = absorption;
    }

    public double getEndurance() {
        return endurance;
    }

    public void setEndurance(double endurance) {
        this.endurance = endurance;
    }

    public double getEnduranceDelay() {
        return enduranceDelay;
    }

    public void setEnduranceDelay(double enduranceDelay) {
        this.enduranceDelay = enduranceDelay;
    }

    public double drain(double enduranceNeeded) {
        boolean drain = false;
        if (getAbsorption() > 0 && enduranceNeeded > 0) {
            if (getAbsorption() > enduranceNeeded) {
                setAbsorption(getAbsorption() - enduranceNeeded);
                enduranceNeeded = 0;
            } else {
                enduranceNeeded -= getAbsorption();
                setAbsorption(0);
            }
            drain = true;
        }
        if (getEndurance() > 0 && enduranceNeeded > 0) {
            if (getEndurance() > enduranceNeeded) {
                setEndurance(getEndurance() - enduranceNeeded);
                enduranceNeeded = 0;
            } else {
                enduranceNeeded -= getEndurance();
                setEndurance(0);
            }
            drain = true;
        }
        if (drain) {
            setEnduranceDelay(5 * 20);
        }
        return enduranceNeeded;
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(EnduranceCapability.class, new EnduranceCapability(), EnduranceCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.ENDURANCE ? Capabilities.ENDURANCE.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.ENDURANCE.getStorage().writeNBT(Capabilities.ENDURANCE, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.ENDURANCE.getStorage().readNBT(Capabilities.ENDURANCE, this, null, nbt);
    }

    //** NBT **//
    public static final String ENDURANCE_NBT = "endurance";
    public static final String ENDURANCE_DELAY_NBT = "endurance_delay";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<EnduranceCapability> capability, EnduranceCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        //Write Endurance
        tag.setDouble(ENDURANCE_NBT, instance.endurance);
        tag.setDouble(ENDURANCE_DELAY_NBT, instance.enduranceDelay);
        return tag;
    }

    @Override
    public void readNBT(Capability<EnduranceCapability> capability, EnduranceCapability instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        //Read endurance
        instance.endurance = tag.getDouble(ENDURANCE_NBT);
        instance.enduranceDelay = tag.getDouble(ENDURANCE_DELAY_NBT);
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "ENDURANCE");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.ENDURANCE.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.ENDURANCE, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.ENDURANCE, null).serializeNBT());
        }
    }
}
