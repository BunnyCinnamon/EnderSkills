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
public class PowerBoostCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<PowerBoostCapability> {

    public float eyeOriginal;
    public float widthOriginal;
    public float heightOriginal;
    public float eyeNew;
    public float widthNew;
    public float heightNew;

    public static void init() {
        CapabilityManager.INSTANCE.register(PowerBoostCapability.class, new PowerBoostCapability(), PowerBoostCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.POWER_BOOST ? Capabilities.POWER_BOOST.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.POWER_BOOST.getStorage().writeNBT(Capabilities.POWER_BOOST, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.POWER_BOOST.getStorage().readNBT(Capabilities.POWER_BOOST, this, null, nbt);
    }

    //** NBT **//
    public static final String EYE_ORIGINAL_NBT = "eyeOriginal";
    public static final String WIDTH_ORIGINAL_NBT = "widthOriginal";
    public static final String HEIGHT_ORIGINAL_NBT = "heightOriginal";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<PowerBoostCapability> capability, PowerBoostCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        //Write Endurance
        tag.setFloat(EYE_ORIGINAL_NBT, instance.eyeOriginal);
        tag.setFloat(WIDTH_ORIGINAL_NBT, instance.heightOriginal);
        tag.setFloat(HEIGHT_ORIGINAL_NBT, instance.heightOriginal);
        return tag;
    }

    @Override
    public void readNBT(Capability<PowerBoostCapability> capability, PowerBoostCapability instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        //Read endurance
        instance.eyeOriginal = tag.getFloat(EYE_ORIGINAL_NBT);
        instance.widthOriginal = tag.getFloat(WIDTH_ORIGINAL_NBT);
        instance.heightOriginal = tag.getFloat(HEIGHT_ORIGINAL_NBT);
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "POWER_BOOST");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.POWER_BOOST.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.POWER_BOOST, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.POWER_BOOST, null).serializeNBT());
        }
    }
}
