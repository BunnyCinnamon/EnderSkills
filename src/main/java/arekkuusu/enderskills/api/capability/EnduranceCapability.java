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

    private final int enduranceDefault = 40;
    private int enduranceDelay;
    private int enduranceMax = enduranceDefault;
    private int endurance;

    public int getEndurance() {
        return endurance;
    }

    public void setEndurance(int endurance) {
        this.endurance = endurance;
    }

    public int getEnduranceDefault() {
        return enduranceDefault;
    }

    public int getEnduranceMax() {
        return enduranceMax;
    }

    public void setEnduranceMax(int enduranceMax) {
        this.enduranceMax = enduranceMax;
    }

    public int getEnduranceDelay() {
        return enduranceDelay;
    }

    public void setEnduranceDelay(int enduranceDelay) {
        this.enduranceDelay = enduranceDelay;
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
    public static final String ENDURANCE_MAX_NBT = "endurance_max";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<EnduranceCapability> capability, EnduranceCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        //Write Endurance
        tag.setInteger(ENDURANCE_NBT, instance.endurance);
        tag.setInteger(ENDURANCE_MAX_NBT, instance.enduranceMax);
        tag.setInteger(ENDURANCE_DELAY_NBT, instance.enduranceDelay);
        return tag;
    }

    @Override
    public void readNBT(Capability<EnduranceCapability> capability, EnduranceCapability instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        //Read endurance
        instance.endurance = tag.getInteger(ENDURANCE_NBT);
        instance.enduranceMax = tag.getInteger(ENDURANCE_MAX_NBT);
        instance.enduranceDelay = tag.getInteger(ENDURANCE_DELAY_NBT);
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
