package arekkuusu.enderskills.api.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public final class NBTHelper {

    /* ItemStack Fixer */
    public static NBTTagCompound fixNBT(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        return tagCompound;
    }

    /* Basic Helpers */
    public static void setByte(NBTTagCompound compound, String tag, byte i) {
        compound.setByte(tag, i);
    }

    public static void setInteger(NBTTagCompound compound, String tag, int i) {
        compound.setInteger(tag, i);
    }

    public static void setFloat(NBTTagCompound compound, String tag, float i) {
        compound.setFloat(tag, i);
    }

    public static void setDouble(NBTTagCompound compound, String tag, double i) {
        compound.setDouble(tag, i);
    }

    public static void setBoolean(NBTTagCompound compound, String tag, boolean i) {
        compound.setBoolean(tag, i);
    }

    public static void setString(NBTTagCompound compound, String tag, String i) {
        compound.setString(tag, i);
    }

    public static void setUUID(NBTTagCompound compound, String tag, UUID i) {
        compound.setUniqueId(tag, i);
    }

    public static byte getByte(NBTTagCompound compound, String tag) {
        return compound.getByte(tag);
    }

    public static int getInteger(NBTTagCompound compound, String tag) {
        return compound.getInteger(tag);
    }

    public static float getFloat(NBTTagCompound compound, String tag) {
        return compound.getFloat(tag);
    }

    public static double getDouble(NBTTagCompound compound, String tag) {
        return compound.getDouble(tag);
    }

    public static boolean getBoolean(NBTTagCompound compound, String tag) {
        return compound.getBoolean(tag);
    }

    public static String getString(NBTTagCompound compound, String tag) {
        return compound.getString(tag);
    }

    @Nullable
    public static UUID getUUID(NBTTagCompound compound, String tag) {
        return compound.hasUniqueId(tag) ? compound.getUniqueId(tag) : null;
    }

    public static <T extends NBTBase> T setNBT(NBTTagCompound compound, String tag, T base) {
        compound.setTag(tag, base);
        return base;
    }

    public static boolean hasTag(NBTTagCompound compound, String tag, int type) {
        return compound != null && compound.hasKey(tag, type);
    }

    public static boolean hasTag(NBTTagCompound compound, String tag) {
        return compound != null && compound.hasKey(tag);
    }

    /* Complex Helpers */
    public static void setArray(NBTTagCompound compound, String tag, String... array) {
        NBTTagList list = new NBTTagList();
        for (String s : array) {
            list.appendTag(new NBTTagString(s));
        }
        compound.setTag(tag, list);
    }

    public static String[] getArray(NBTTagCompound compound, String tag) {
        NBTTagList list = compound.getTagList(tag, Constants.NBT.TAG_STRING);
        String[] array = new String[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            array[i] = list.getStringTagAt(i);
        }
        return array;
    }

    public static void setBlockPos(NBTTagCompound compound, String tag, @Nullable BlockPos pos) {
        if (pos == null) pos = BlockPos.ORIGIN;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        compound.setTag(tag, nbt);
    }

    public static BlockPos getBlockPos(NBTTagCompound compound, String tag) {
        BlockPos pos = BlockPos.ORIGIN;
        if (hasTag(compound, tag, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound nbt = compound.getCompoundTag(tag);
            int x = nbt.getInteger("x");
            int y = nbt.getInteger("y");
            int z = nbt.getInteger("z");
            pos = pos.add(x, y, z);
        }
        return pos;
    }

    public static void setVector(NBTTagCompound compound, String tag, Vec3d vec) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("x", vec.x);
        nbt.setDouble("y", vec.y);
        nbt.setDouble("z", vec.z);
        compound.setTag(tag, nbt);
    }

    public static Vec3d getVector(NBTTagCompound compound, String tag) {
        Vec3d vec = Vec3d.ZERO;
        if (hasTag(compound, tag, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound nbt = compound.getCompoundTag(tag);
            double x = nbt.getDouble("x");
            double y = nbt.getDouble("y");
            double z = nbt.getDouble("z");
            vec = vec.addVector(x, y, z);
        }
        return vec;
    }

    public static <T extends IForgeRegistryEntry<T>> void setRegistry(NBTTagCompound compound, String tag, IForgeRegistryEntry.Impl<T> instance) {
        setResourceLocation(compound, tag, Objects.requireNonNull(instance.getRegistryName()));
    }

    public static <T extends IForgeRegistryEntry.Impl<T>> T getRegistry(NBTTagCompound compound, String tag, Class<T> registry) {
        ResourceLocation location = getResourceLocation(compound, tag);
        return GameRegistry.findRegistry(registry).getValue(location);
    }

    public static void setResourceLocation(NBTTagCompound compound, String tag, ResourceLocation location) {
        compound.setString(tag, location.toString());
    }

    public static ResourceLocation getResourceLocation(NBTTagCompound compound, String tag) {
        return new ResourceLocation(compound.getString(tag));
    }

    public static void setWorld(NBTTagCompound compound, String tag, World world) {
        compound.setInteger(tag, world.provider.getDimension());
    }

    public static World getWorld(NBTTagCompound compound, String tag) {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                ? DimensionManager.getWorld(compound.getInteger(tag))
                : Minecraft.getMinecraft().player.world;
    }

    public static <T extends Enum<T> & IStringSerializable> void setEnum(NBTTagCompound compound, T t, String tag) {
        compound.setString(tag, t.getName());
    }

    public static <T extends Enum<T> & IStringSerializable> Optional<T> getEnum(Class<T> clazz, NBTTagCompound compound, String tag) {
        String value = compound.getString(tag);
        return Stream.of(clazz.getEnumConstants()).filter(e -> e.getName().equals(value)).findAny();
    }

    public static NBTTagCompound getNBTTag(NBTTagCompound compound, String tag) {
        return hasTag(compound, tag, Constants.NBT.TAG_COMPOUND) ? compound.getCompoundTag(tag) : new NBTTagCompound();
    }

    public static Optional<NBTTagList> getNBTList(NBTTagCompound compound, String tag) {
        return hasTag(compound, tag, Constants.NBT.TAG_LIST) ? Optional.of(compound.getTagList(tag, Constants.NBT.TAG_COMPOUND)) : Optional.empty();
    }

    public static void setEntity(NBTTagCompound compound, Entity entity, String subtag) {
        NBTTagCompound nbt = getNBTTag(compound, "entity");
        NBTTagCompound tag = getNBTTag(nbt, subtag);
        setUUID(tag, "uuid", entity.getUniqueID());
        setWorld(tag, "world", entity.getEntityWorld());
        setNBT(nbt, subtag, tag);
        setNBT(compound, "entity", nbt);
    }

    @Nullable
    public static <T extends Entity> T getEntity(Class<T> ent, NBTTagCompound compound, String subtag) {
        NBTTagCompound nbt = getNBTTag(compound, "entity");
        NBTTagCompound tag = getNBTTag(nbt, subtag);
        UUID u = getUUID(tag, "uuid");
        World w = getWorld(tag, "world");
        return WorldHelper.getEntity(ent, w, u);
    }

    public static boolean hasUniqueID(NBTTagCompound compound, String tag) {
        return compound != null && compound.hasUniqueId(tag);
    }

    public static void removeTag(NBTTagCompound compound, String tag) {
        if (compound != null && compound.hasKey(tag)) {
            compound.removeTag(tag);
        }
    }
}
